import express from 'express';
import { KafkaProducer } from '../_shared/kafka/producer';
import { KafkaConsumer } from '../_shared/kafka/consumer';
import { initTracer } from '../_shared/otel/tracer';
import { initMetrics } from '../_shared/otel/metrics';

const app = express();
const port = 7093;

// Initialize OpenTelemetry
initTracer('nlg-service', '1.0.0', process.env.JARVIS_ENV || 'dev');
const meter = initMetrics('nlg-service', process.env.JARVIS_ENV || 'dev');

// Kafka setup
const producer = new KafkaProducer(
  (process.env.KAFKA_BROKERS || 'kafka:9092').split(','),
  process.env.SCHEMA_REGISTRY_URL || 'http://schema-registry:8081'
);

const consumer = new KafkaConsumer(
  (process.env.KAFKA_BROKERS || 'kafka:9092').split(','),
  process.env.SCHEMA_REGISTRY_URL || 'http://schema-registry:8081',
  'nlg-service-group'
);

// Metrics
const nlgGenLatencyHistogram = meter.createHistogram('nlg_gen_latency_ms', {
  description: 'NLG response generation latency in milliseconds'
});

const responsesGenerated = meter.createCounter('responses_generated', {
  description: 'Number of responses generated'
});

const responsesByIntent = meter.createCounter('responses_by_intent', {
  description: 'Number of responses by intent type'
});

// Response templates with variations
const responseTemplates = {
  GREETING: [
    "Hello! How can I help you today?",
    "Hi there! What would you like me to do?",
    "Greetings! I'm here to assist you.",
    "Hey! What can I do for you?",
    "Good to see you! How may I help?"
  ],
  ECHO: [
    "You said: {text}",
    "I heard: {text}",
    "You mentioned: {text}",
    "You told me: {text}",
    "You said: {text}"
  ],
  TIME_NOW: [
    "The current time is {time}",
    "It's {time} right now",
    "The time is currently {time}",
    "Right now it's {time}",
    "It's {time} at the moment"
  ],
  WEATHER_SIMPLE: [
    "I can't check the weather right now, but I'd be happy to help with other tasks!",
    "Weather information isn't available at the moment. Is there something else I can assist you with?",
    "I don't have access to weather data currently. What else can I help you with?",
    "Weather checking isn't working right now. How else can I be of service?",
    "I'm unable to get weather info at the moment. What other tasks can I help with?"
  ],
  UNKNOWN: [
    "I'm not sure I understood that. Could you rephrase it?",
    "I didn't quite catch that. Can you say it differently?",
    "I'm not certain what you mean. Could you try again?",
    "That's not clear to me. Can you explain it another way?",
    "I'm confused by that request. Could you clarify?"
  ]
};

function generateResponse(intent: string, slots: Record<string, any> = {}): { text: string; ssml: string } {
  const templates = responseTemplates[intent as keyof typeof responseTemplates] || responseTemplates.UNKNOWN;
  
  // Pick random template for variation
  const template = templates[Math.floor(Math.random() * templates.length)];
  
  // Replace placeholders
  let text = template;
  if (intent === 'ECHO' && slots.text) {
    text = template.replace('{text}', slots.text);
  } else if (intent === 'TIME_NOW') {
    const now = new Date();
    const time = now.toLocaleTimeString('en-US', { 
      hour: 'numeric', 
      minute: '2-digit',
      hour12: true 
    });
    text = template.replace('{time}', time);
  }
  
  // Generate SSML (optional, for TTS enhancement)
  let ssml = `<speak>${text}</speak>`;
  
  // Add SSML enhancements for specific intents
  if (intent === 'GREETING') {
    ssml = `<speak><prosody rate="medium" pitch="+1st">${text}</prosody></speak>`;
  } else if (intent === 'ECHO') {
    ssml = `<speak><emphasis level="moderate">${text}</emphasis></speak>`;
  } else if (intent === 'TIME_NOW') {
    ssml = `<speak><prosody rate="slow">${text}</prosody></speak>`;
  }
  
  return { text, ssml };
}

async function processIntentMessage(message: any) {
  const startTime = Date.now();
  
  try {
    const intent = message.intent;
    const slots = message.slots || {};
    const sessionId = message.sessionId;
    const correlationId = message.correlationId;
    const lang = message.lang || 'en';
    
    if (!intent || !sessionId || !correlationId) {
      console.warn('Missing required fields in intent message:', message);
      return;
    }
    
    // Generate response
    const response = generateResponse(intent, slots);
    
    // Record metrics
    responsesGenerated.add(1);
    responsesByIntent.add(1, { intent });
    
    // Publish response to Kafka
    const responseMessage = {
      messageVersion: 1,
      timestamp: Date.now(),
      correlationId,
      sessionId,
      text: response.text,
      ssml: response.ssml,
      intent,
      slots,
      lang,
      errors: []
    };
    
    await producer.send('sj.nlg.reply.v1', sessionId, responseMessage);
    
    // Record latency
    const latency = Date.now() - startTime;
    nlgGenLatencyHistogram.record(latency);
    
    console.log(`Response generated for intent ${intent}: "${response.text}"`);
    
  } catch (error) {
    console.error('Error processing intent message:', error);
    
    // Send to DLQ
    try {
      const dlqMessage = {
        originalTopic: 'sj.nlu.intent.v1',
        error: (error as Error).message,
        timestamp: Date.now(),
        payload: message
      };
      await producer.send('sj.nlg.dlq.v1', message.sessionId || 'unknown', dlqMessage);
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
    await consumer.subscribe('sj.nlu.intent.v1', async (payload) => {
      try {
        const message = JSON.parse(payload.message.value?.toString() || '{}');
        await processIntentMessage(message);
      } catch (error) {
        console.error('Error parsing intent message:', error);
      }
    });
    
    console.log('NLG Service listening to sj.nlu.intent.v1');
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
    
    console.log('NLG Service started successfully');
  } catch (error) {
    console.error('Failed to start NLG Service:', error);
    process.exit(1);
  }
}

start();

// Graceful shutdown
process.on('SIGTERM', async () => {
  console.log('Shutting down NLG Service...');
  await producer.disconnect();
  await consumer.disconnect();
  process.exit(0);
});

// Start HTTP server
app.listen(port, () => {
  console.log(`NLG Service listening on port ${port}`);
});
