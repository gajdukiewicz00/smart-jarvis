import { IntentHandler } from '../intents/IntentHandler';

export interface IntentResult {
    intent: string;
    confidence: number;
    entities: Record<string, any>;
    response: string;
}

export class IntentProcessor {
    private handlers: Map<string, IntentHandler> = new Map();
    private supportedIntents: string[] = [];

    registerHandler(intent: string, handler: IntentHandler): void {
        this.handlers.set(intent, handler);
        this.supportedIntents.push(intent);
    }

    async processIntent(text: string, context?: any): Promise<IntentResult> {
        const lowerText = text.toLowerCase();
        
        // Simple intent recognition based on keywords
        if (lowerText.includes('create') || lowerText.includes('add') || lowerText.includes('new')) {
            if (lowerText.includes('task')) {
                return this.handleIntent('task', text, context);
            } else if (lowerText.includes('reminder')) {
                return this.handleIntent('reminder', text, context);
            }
        } else if (lowerText.includes('list') || lowerText.includes('show') || lowerText.includes('get')) {
            if (lowerText.includes('task')) {
                return this.handleIntent('task', text, context);
            }
        } else if (lowerText.includes('delete') || lowerText.includes('remove')) {
            if (lowerText.includes('task')) {
                return this.handleIntent('task', text, context);
            } else if (lowerText.includes('reminder')) {
                return this.handleIntent('reminder', text, context);
            }
        } else if (lowerText.includes('system') || lowerText.includes('info')) {
            return this.handleIntent('system', text, context);
        }

        // Default to system intent
        return this.handleIntent('system', text, context);
    }

    private async handleIntent(intent: string, text: string, context?: any): Promise<IntentResult> {
        const handler = this.handlers.get(intent);
        
        if (handler) {
            return await handler.handle(text, context);
        }

        // Default response if no handler found
        return {
            intent: intent,
            confidence: 0.5,
            entities: {},
            response: `I understand you want to ${intent}, but I'm not sure how to help with that yet.`
        };
    }

    getSupportedIntents(): string[] {
        return [...this.supportedIntents];
    }
} 