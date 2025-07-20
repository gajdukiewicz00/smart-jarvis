import { IntentProcessor, IntentResult } from './IntentProcessor';

export class NLPService {
    private intentProcessor: IntentProcessor;

    constructor(intentProcessor: IntentProcessor) {
        this.intentProcessor = intentProcessor;
    }

    async processIntent(text: string, context?: any): Promise<IntentResult> {
        return await this.intentProcessor.processIntent(text, context);
    }
} 