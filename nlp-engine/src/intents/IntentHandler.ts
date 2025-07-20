export interface IntentResult {
    intent: string;
    confidence: number;
    entities: Record<string, any>;
    response: string;
}

export interface IntentHandler {
    handle(text: string, context?: any): Promise<IntentResult>;
} 