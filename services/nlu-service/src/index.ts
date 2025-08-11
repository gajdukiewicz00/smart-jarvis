import express from 'express';
import { KafkaProducer } from '../_shared/kafka/producer';
import { KafkaConsumer } from '../_shared/kafka/consumer';
import { initTracer } from '../_shared/otel/tracer';
import { initMetrics } from '../_shared/otel/metrics';

const app = express();
const port = 7092;

// Initialize OpenTelemetry
initTracer('nlu-service', '1.0.0', process.env.JARVIS_ENV || 'dev');
const meter = initMetrics('nlu-service', process.env.JARVIS_ENV || 'dev');

// Kafka setup
const producer = new KafkaProducer(
  (process.env.KAFKA_BROKERS || 'kafka:9092').split(','),
  process.env.SCHEMA_REGISTRY_URL || 'http://schema-registry:8081'
);

const consumer = new KafkaConsumer(
  (process.env.KAFKA_BROKERS || 'kafka:9092').split(','),
  process.env.SCHEMA_REGISTRY_URL || 'http://schema-registry:8081',
  'nlu-service-group'
);

// Metrics
const nluLatencyHistogram = meter.createHistogram('nlu_latency_ms', {
  description: 'NLU processing latency in milliseconds'
});

const intentsProcessed = meter.createCounter('intents_processed', {
  description: 'Number of intents processed'
});

const intentsByType = meter.createCounter('intents_by_type', {
  description: 'Number of intents by type'
});

// Intent recognition rules
const intentPatterns = [
  {
    intent: 'GREETING',
    patterns: [
      /^(hi|hello|hey|good morning|good afternoon|good evening)/i,
      /^(what's up|how are you|howdy)/i,
      /^(greetings|salutations)/i
    ],
    confidence: 0.9
  },
  {
    intent: 'ECHO',
    patterns: [
      /^(echo|repeat|say|tell me)/i,
      /^(can you say|please say)/i
    ],
    confidence: 0.8
  },
  {
    intent: 'TIME_NOW',
    patterns: [
      /^(what time|what's the time|current time|time now)/i,
      /^(tell me the time|what time is it)/i
    ],
    confidence: 0.9
  },
  {
    intent: 'WEATHER_SIMPLE',
    patterns: [
      /^(weather|temperature|how's the weather|is it raining)/i,
      /^(weather forecast|weather today)/i
    ],
    confidence: 0.8
  }
];

function recognizeIntent(text: string): { intent: string; confidence: number; slots: Record<string, any> } {
  const normalizedText = text.toLowerCase().trim();
  
  // Check patterns
  for (const pattern of intentPatterns) {
    for (const regex of pattern.patterns) {
      if (regex.test(normalizedText)) {
        // Extract slots for specific intents
        let slots: Record<string, any> = {};
        
        if (pattern.intent === 'ECHO') {
          const echoMatch = normalizedText.match(/^(echo|repeat|say|tell me|can you say|please say)\s+(.+)/i);
          if (echoMatch) {
            slots = { text: echoMatch[2] };
          }
        }
        
        return {
          intent: pattern.intent,
          confidence: pattern.confidence,
          slots
        };
      }
    }
  }
  
  // Default to UNKNOWN
  return {
    intent: 'UNKNOWN',
    confidence: 0.1,
    slots: {}
  };
}

async function processSttMessage(message: any) {
  const startTime = Date.now();
  
  try {
    const text = message.text;
    const sessionId = message.sessionId;
    const correlationId = message.correlationId;
    const lang = message.lang || 'en';
    
    if (!text || !sessionId || !correlationId) {
      console.warn('Missing required fields in STT message:', message);
      return;
    }
    
    // Recognize intent
    const intentResult = recognizeIntent(text);
    
    // Record metrics
    intentsProcessed.add(1);
    intentsByType.add(1, { intent: intentResult.intent });
    
    // Publish intent to Kafka
    const intentMessage = {
      messageVersion: 1,
      timestamp: Date.now(),
      correlationId,
      sessionId,
      intent: intentResult.intent,
      slots: intentResult.slots,
      confidence: intentResult.confidence,
      originalText: text,
      lang,
      errors: []
    };
    
    await producer.send('sj.nlu.intent.v1', sessionId, intentMessage);
    
    // Record latency
    const latency = Date.now() - startTime;
    nluLatencyHistogram.record(latency);
    
    console.log(`Intent recognized: ${intentResult.intent} (${intentResult.confidence}) for text: "${text}"`);
    
  } catch (error) {
    console.error('Error processing STT message:', error);
    
    // Send to DLQ
    try {
      const dlqMessage = {
        originalTopic: 'sj.stt.final.v1',
        error: (error as Error).message,
        timestamp: Date.now(),
        payload: message
      };
      await producer.send('sj.nlu.dlq.v1', message.sessionId || 'unknown', dlqMessage);
    } catch (dlqError) {
      console.error('Failed to send to DLQ:', dlqError);
    }
  }
}

// Health endpoints
app.get('/healthz', (req, res) => res.status(200).send('OK'));
app.get('/readyz', async (req, res) => {
  const okKafka = await producer.isReady() && await consumer.isReady();
  // TODO: Add Schema Registry readiness check
  res.status(okKafka ? 200 : 503).send({ okKafka });
});

// Kafka consumer setup
async function setupConsumer() {
  try {
    await consumer.subscribe('sj.stt.final.v1', async (payload) => {
      try {
        const message = JSON.parse(payload.message.value?.toString() || '{}');
        await processSttMessage(message);
      } catch (error) {
        console.error('Error parsing STT message:', error);
      }
    });
    
    console.log('NLU Service listening to sj.stt.final.v1');
  } catch (error) {
    console.error('Failed to setup Kafka consumer:', error);
    throw error;
  }
}

// Startup
async function start() {
  try {
    await producer.connect();
    await consumer.connect();
    await setupConsumer();
    
    console.log('NLU Service started successfully');
  } catch (error) {
    console.error('Failed to start NLU Service:', error);
    process.exit(1);
  }
}

start();

// Graceful shutdown
process.on('SIGTERM', async () => {
  console.log('Shutting down NLU Service...');
  await producer.disconnect();
  await consumer.disconnect();
  process.exit(0);
});

// Start HTTP server
app.listen(port, () => {
  console.log(`NLU Service listening on port ${port}`);
});
