import express from 'express';
import { KafkaProducer } from '../_shared/kafka/producer';
import { KafkaConsumer } from '../_shared/kafka/consumer';
import { initTracer } from '../_shared/otel/tracer';
import { initMetrics } from '../_shared/otel/metrics';

const app = express();
const port = 7095;

// Initialize OpenTelemetry
initTracer('dm-service', '1.0.0', process.env.JARVIS_ENV || 'dev');
const meter = initMetrics('dm-service', process.env.JARVIS_ENV || 'dev');

// Kafka setup
const producer = new KafkaProducer(
  (process.env.KAFKA_BROKERS || 'kafka:9092').split(','),
  process.env.SCHEMA_REGISTRY_URL || 'http://schema-registry:8081'
);

const consumer = new KafkaConsumer(
  (process.env.KAFKA_BROKERS || 'kafka:9092').split(','),
  process.env.SCHEMA_REGISTRY_URL || 'http://schema-registry:8081',
  'dm-service-group'
);

// Metrics
const dmLatencyHistogram = meter.createHistogram('dm_latency_ms', {
  description: 'Dialog Manager processing latency in milliseconds'
});

const stateTransitions = meter.createCounter('state_transitions', {
  description: 'Number of state transitions'
});

const sessionsActive = meter.createUpDownCounter('sessions_active', {
  description: 'Number of active sessions'
});

// Dialog Manager FSM
enum DialogState {
  IDLE = 'idle',
  LISTENING = 'listening',
  UNDERSTANDING = 'understanding',
  SPEAKING = 'speaking',
}

interface Session {
  sessionId: string;
  correlationId: string;
  state: DialogState;
  lastIntent?: any;
  lastResponse?: any;
  lastUpdate: number;
}

const sessions = new Map<string, Session>();

function createSession(sessionId: string, correlationId: string): Session {
  const session: Session = {
    sessionId,
    correlationId,
    state: DialogState.IDLE,
    lastUpdate: Date.now()
  };
  
  sessions.set(sessionId, session);
  sessionsActive.add(1);
  console.log(`Created session: ${sessionId}`);
  
  return session;
}

function updateSessionState(sessionId: string, newState: DialogState) {
  const session = sessions.get(sessionId);
  if (session) {
    const oldState = session.state;
    session.state = newState;
    session.lastUpdate = Date.now();
    
    stateTransitions.add(1, { from: oldState, to: newState });
    console.log(`Session ${sessionId}: ${oldState} → ${newState}`);
  }
}

function cleanupSession(sessionId: string) {
  if (sessions.has(sessionId)) {
    sessions.delete(sessionId);
    sessionsActive.add(-1);
    console.log(`Cleaned up session: ${sessionId}`);
  }
}

// State machine logic
async function handleStateTransition(session: Session, event: string, data: any) {
  const startTime = Date.now();
  
  try {
    switch (session.state) {
      case DialogState.IDLE:
        if (event === 'audio_received') {
          updateSessionState(session.sessionId, DialogState.LISTENING);
        }
        break;
        
      case DialogState.LISTENING:
        if (event === 'intent_recognized') {
          session.lastIntent = data;
          updateSessionState(session.sessionId, DialogState.UNDERSTANDING);
          
          // Request NLG response
          await producer.send('sj.nlg.reply.v1', session.sessionId, {
            messageVersion: 1,
            timestamp: Date.now(),
            correlationId: session.correlationId,
            sessionId: session.sessionId,
            intent: data.intent,
            slots: data.slots,
            lang: data.lang || 'en',
            errors: []
          });
        }
        break;
        
      case DialogState.UNDERSTANDING:
        if (event === 'response_generated') {
          session.lastResponse = data;
          updateSessionState(session.sessionId, DialogState.SPEAKING);
          
          // Request TTS synthesis
          await producer.send('sj.tts.request.v1', session.sessionId, {
            messageVersion: 1,
            timestamp: Date.now(),
            correlationId: session.correlationId,
            sessionId: session.sessionId,
            text: data.text,
            ssml: data.ssml,
            voice: data.voice,
            errors: []
          });
        }
        break;
        
      case DialogState.SPEAKING:
        if (event === 'audio_synthesized') {
          // Reset to idle for next interaction
          updateSessionState(session.sessionId, DialogState.IDLE);
        }
        break;
    }
    
    // Record latency
    const latency = Date.now() - startTime;
    dmLatencyHistogram.record(latency);
    
  } catch (error) {
    console.error(`Error in state transition for session ${session.sessionId}:`, error);
    
    // Send to DLQ
    try {
      const dlqMessage = {
        originalTopic: 'dm-service',
        error: (error as Error).message,
        timestamp: Date.now(),
        sessionId: session.sessionId,
        state: session.state,
        event,
        data
      };
      await producer.send('sj.dm.dlq.v1', session.sessionId, dlqMessage);
    } catch (dlqError) {
      console.error('Failed to send to DLQ:', dlqError);
    }
  }
}

// Process intent messages from NLU
async function processIntentMessage(message: any) {
  try {
    const sessionId = message.sessionId;
    const correlationId = message.correlationId;
    
    if (!sessionId || !correlationId) {
      console.warn('Missing required fields in intent message:', message);
      return;
    }
    
    // Get or create session
    let session = sessions.get(sessionId);
    if (!session) {
      session = createSession(sessionId, correlationId);
    }
    
    // Handle intent recognition
    await handleStateTransition(session, 'intent_recognized', message);
    
  } catch (error) {
    console.error('Error processing intent message:', error);
  }
}

// Process response messages from NLG
async function processResponseMessage(message: any) {
  try {
    const sessionId = message.sessionId;
    
    if (!sessionId) {
      console.warn('Missing sessionId in response message:', message);
      return;
    }
    
    const session = sessions.get(sessionId);
    if (session) {
      await handleStateTransition(session, 'response_generated', message);
    }
    
  } catch (error) {
    console.error('Error processing response message:', error);
  }
}

// Process TTS audio messages
async function processTtsAudioMessage(message: any) {
  try {
    const sessionId = message.sessionId;
    
    if (!sessionId) {
      console.warn('Missing sessionId in TTS audio message:', message);
      return;
    }
    
    const session = sessions.get(sessionId);
    if (session) {
      await handleStateTransition(session, 'audio_synthesized', message);
    }
    
  } catch (error) {
    console.error('Error processing TTS audio message:', error);
  }
}

// Health endpoints
app.get('/healthz', (req, res) => res.status(200).send('OK'));
app.get('/readyz', async (req, res) => {
  const okKafka = await producer.isReady() && await consumer.isReady();
  // TODO: Add Schema Registry readiness check
  res.status(okKafka ? 200 : 503).send({ okKafka });
});

// Session management endpoint
app.get('/sessions', (req, res) => {
  const sessionList = Array.from(sessions.values()).map(s => ({
    sessionId: s.sessionId,
    state: s.state,
    lastUpdate: s.lastUpdate,
    lastIntent: s.lastIntent?.intent,
    lastResponse: s.lastResponse?.text
  }));
  
  res.json({
    activeSessions: sessionList.length,
    sessions: sessionList
  });
});

// Kafka consumer setup
async function setupConsumers() {
  try {
    // Listen to NLU intents
    await consumer.subscribe('sj.nlu.intent.v1', async (payload) => {
      try {
        const message = JSON.parse(payload.message.value?.toString() || '{}');
        await processIntentMessage(message);
      } catch (error) {
        console.error('Error parsing intent message:', error);
      }
    });
    
    // Listen to NLG responses
    await consumer.subscribe('sj.nlg.reply.v1', async (payload) => {
      try {
        const message = JSON.parse(payload.message.value?.toString() || '{}');
        await processResponseMessage(message);
      } catch (error) {
        console.error('Error parsing response message:', error);
      }
    });
    
    // Listen to TTS audio
    await consumer.subscribe('sj.tts.audio.v1', async (payload) => {
      try {
        const message = JSON.parse(payload.message.value?.toString() || '{}');
        await processTtsAudioMessage(message);
      } catch (error) {
        console.error('Error parsing TTS audio message:', error);
      }
    });
    
    console.log('DM Service listening to sj.nlu.intent.v1, sj.nlg.reply.v1, sj.tts.audio.v1');
  } catch (error) {
    console.error('Failed to setup Kafka consumers:', error);
    throw error;
  }
}

// Cleanup old sessions periodically
setInterval(() => {
  const now = Date.now();
  const maxAge = 5 * 60 * 1000; // 5 minutes
  
  for (const [sessionId, session] of sessions.entries()) {
    if (now - session.lastUpdate > maxAge) {
      cleanupSession(sessionId);
    }
  }
}, 60000); // Check every minute

// Startup
async function start() {
  try {
    await producer.connect();
    await consumer.connect();
    await setupConsumers();
    
    console.log('DM Service started successfully');
  } catch (error) {
    console.error('Failed to start DM Service:', error);
    process.exit(1);
  }
}

start();

// Graceful shutdown
process.on('SIGTERM', async () => {
  console.log('Shutting down DM Service...');
  await producer.disconnect();
  await consumer.disconnect();
  process.exit(0);
});

// Start HTTP server
app.listen(port, () => {
  console.log(`DM Service listening on port ${port}`);
});
