import { Kafka, Producer } from 'kafkajs';

export class KafkaProducer {
  private producer: Producer;

  constructor(brokers: string[], registryUrl: string) {
    const kafka = new Kafka({ brokers });
    this.producer = kafka.producer();
  }

  async connect() {
    await this.producer.connect();
  }

  async disconnect() {
    await this.producer.disconnect();
  }

  async send(topic: string, key: string, value: any) {
    const message = {
      key: Buffer.from(key),
      value: JSON.stringify(value),
      headers: {
        'correlation-id': Buffer.from(value.correlationId || ''),
        'session-id': Buffer.from(value.sessionId || ''),
        'timestamp': Buffer.from(Date.now().toString())
      }
    };
    
    await this.producer.send({ topic, messages: [message] });
  }

  async isReady(): Promise<boolean> {
    try {
      await this.producer.send({ topic: '__health_check', messages: [] });
      return true;
    } catch (error) {
      console.error('Kafka Producer health check failed:', error);
      return false;
    }
  }
}
