import express from 'express';
import { WebSocketServer, WebSocket } from 'ws';
import { KafkaProducer } from '../_shared/kafka/producer';
import { KafkaConsumer } from '../_shared/kafka/consumer';
import { initTracer } from '../_shared/otel/tracer';
import { initMetrics } from '../_shared/otel/metrics';

const app = express();
const port = process.env.VOICE_GATEWAY_PORT || 7090;

// Initialize OpenTelemetry
initTracer('voice-gateway', '1.0.0', process.env.JARVIS_ENV || 'dev');
const meter = initMetrics('voice-gateway', process.env.JARVIS_ENV || 'dev');

// Kafka setup
const producer = new KafkaProducer(
  (process.env.KAFKA_BROKERS || 'kafka:9092').split(','),
  process.env.SCHEMA_REGISTRY_URL || 'http://schema-registry:8081'
);

const consumer = new KafkaConsumer(
  (process.env.KAFKA_BROKERS || 'kafka:9092').split(','),
  process.env.SCHEMA_REGISTRY_URL || 'http://schema-registry:8081',
  'voice-gateway-group'
);

// Metrics
const voiceRoundtripHistogram = meter.createHistogram('voice_roundtrip_ms', {
  description: 'Voice roundtrip latency in milliseconds'
});

const audioChunksProcessed = meter.createCounter('audio_chunks_processed', {
  description: 'Number of audio chunks processed'
});

const audioChunksDropped = meter.createCounter('audio_chunks_dropped', {
  description: 'Number of audio chunks dropped due to lag'
});

// WebSocket header parsing
type Header = { sessionId: string; correlationId: string; sampleRate: number; protocolVersion: number };
function parseHeader(buf: Buffer): Header {
  try {
    const h = JSON.parse(buf.toString("utf8"));
    if (!h.sessionId || !h.correlationId || h.sampleRate !== 16000) throw new Error("bad header");
    if (typeof h.protocolVersion !== "number") h.protocolVersion = 1;
    return h;
  } catch { throw new Error("invalid first frame"); }
}

// Session management with bounded queues
interface Session {
  ws: WebSocket;
  header: Header;
  audioQueue: Buffer[];
  ttsQueue: Buffer[];
  lastAudioTime: number;
  lastTtsTime: number;
  heartbeatInterval: NodeJS.Timeout;
}

const sessions = new Map<string, Session>();
const MAX_QUEUE_SIZE = 50; // 50 chunks = ~1 second of audio

function createSession(ws: WebSocket, header: Header): Session {
  const session: Session = {
    ws,
    header,
    audioQueue: [],
    ttsQueue: [],
    lastAudioTime: Date.now(),
    lastTtsTime: Date.now(),
    heartbeatInterval: setInterval(() => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.ping();
      }
    }, 5000)
  };
  
  sessions.set(header.sessionId, session);
  return session;
}

function cleanupSession(sessionId: string) {
  const session = sessions.get(sessionId);
  if (session) {
    clearInterval(session.heartbeatInterval);
    sessions.delete(sessionId);
  }
}

// Health and Readiness endpoints
app.get('/healthz', (req, res) => res.status(200).send('OK'));
app.get('/readyz', async (req, res) => {
  const okKafka = await producer.isReady() && await consumer.isReady();
  // TODO: Add Schema Registry readiness check
  res.status(okKafka ? 200 : 503).send({ okKafka });
});

// WebSocket server
const wss = new WebSocketServer({ noServer: true });

wss.on('connection', (ws: WebSocket, request: any) => {
  let session: Session | null = null;
  let headerReceived = false;

  ws.on('message', async (data: Buffer) => {
    try {
      if (!headerReceived) {
        // First frame: JSON header
        const header = parseHeader(data);
        session = createSession(ws, header);
        headerReceived = true;
        
        console.log(`New session: ${header.sessionId}`);
        return;
      }

      if (!session) return;

      // Subsequent frames: PCM16 audio chunks
      const now = Date.now();
      session.lastAudioTime = now;

      // Add to bounded queue
      if (session.audioQueue.length >= MAX_QUEUE_SIZE) {
        session.audioQueue.shift(); // Drop oldest
        audioChunksDropped.add(1);
        console.warn(`Dropped audio chunk for session ${session.header.sessionId} due to queue overflow`);
      }
      session.audioQueue.push(data);

      // Publish to Kafka
      await producer.send('sj.audio.raw.v1', session.header.sessionId, {
        messageVersion: 1,
        timestamp: now,
        correlationId: session.header.correlationId,
        sessionId: session.header.sessionId,
        audioData: data.toString('base64'),
        sampleRate: 16000,
        format: 'PCM16',
        errors: []
      });

      audioChunksProcessed.add(1);

    } catch (error) {
      console.error('Error processing message:', error);
      ws.close(1008, 'Invalid message format');
    }
  });

  ws.on('close', () => {
    if (session) {
      cleanupSession(session.header.sessionId);
      console.log(`Session closed: ${session.header.sessionId}`);
    }
  });

  ws.on('error', (error) => {
    console.error('WebSocket error:', error);
    if (session) {
      cleanupSession(session.header.sessionId);
    }
  });
});

// Kafka consumer for TTS audio
async function setupTtsConsumer() {
  await consumer.subscribe('sj.tts.audio.v1', async (payload) => {
    try {
      const value = JSON.parse(payload.message.value?.toString() || '{}');
      const sessionId = value.sessionId;
      
      const session = sessions.get(sessionId);
      if (session && session.ws.readyState === WebSocket.OPEN) {
        // Decode base64 audio and send to client
        const audioData = Buffer.from(value.audioData, 'base64');
        
        // Add to bounded TTS queue
        if (session.ttsQueue.length >= MAX_QUEUE_SIZE) {
          session.ttsQueue.shift();
          console.warn(`Dropped TTS chunk for session ${sessionId} due to queue overflow`);
        }
        session.ttsQueue.push(audioData);
        
        // Send to WebSocket client
        session.ws.send(audioData);
        session.lastTtsTime = Date.now();
        
        // Calculate roundtrip time
        const roundtripTime = session.lastTtsTime - session.lastAudioTime;
        voiceRoundtripHistogram.record(roundtripTime);
        
        console.log(`TTS audio sent to session ${sessionId}, roundtrip: ${roundtripTime}ms`);
      }
    } catch (error) {
      console.error('Error processing TTS audio:', error);
    }
  });
}

// Startup
async function start() {
  try {
    await producer.connect();
    await consumer.connect();
    await setupTtsConsumer();
    
    console.log('Voice Gateway started successfully');
  } catch (error) {
    console.error('Failed to start Voice Gateway:', error);
    process.exit(1);
  }
}

start();

// Graceful shutdown
process.on('SIGTERM', async () => {
  console.log('Shutting down Voice Gateway...');
  await producer.disconnect();
  await consumer.disconnect();
  process.exit(0);
});

// Start HTTP server
const server = app.listen(port, () => {
  console.log(`Voice Gateway listening on port ${port}`);
});

// Attach WebSocket server to HTTP server
server.on('upgrade', (request, socket, head) => {
  wss.handleUpgrade(request, socket, head, (ws) => {
    wss.emit('connection', ws, request);
  });
});
