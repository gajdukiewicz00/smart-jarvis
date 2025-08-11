import { Kafka, Consumer, EachMessagePayload } from 'kafkajs';
import { SchemaRegistry } from '@kafkajs/confluent-schema-registry';

export class KafkaConsumer {
  private consumer: Consumer;
  private registry: SchemaRegistry;

  constructor(brokers: string[], registryUrl: string, groupId: string) {
    const kafka = new Kafka({ brokers });
    this.consumer = kafka.consumer({ groupId });
    this.registry = new SchemaRegistry({ host: registryUrl });
  }

  async connect() {
    await this.consumer.connect();
  }

  async disconnect() {
    await this.consumer.disconnect();
  }

  async subscribe(topic: string, handler: (payload: EachMessagePayload) => Promise<void>) {
    await this.consumer.subscribe({ topic });
    await this.consumer.run({
      eachMessage: async (payload) => {
        try {
          await handler(payload);
        } catch (error) {
          console.error('Message processing error:', error);
          // TODO: Send to DLQ
        }
      }
    });
  }

  async isReady(): Promise<boolean> {
    try {
      await this.consumer.describeGroup();
      return true;
    } catch {
      return false;
    }
  }
}
