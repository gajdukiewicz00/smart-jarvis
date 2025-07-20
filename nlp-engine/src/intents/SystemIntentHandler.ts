import { IntentHandler, IntentResult } from './IntentHandler';

export class SystemIntentHandler implements IntentHandler {
    
    async handle(text: string, context?: any): Promise<IntentResult> {
        const lowerText = text.toLowerCase();
        
        if (lowerText.includes('system') || lowerText.includes('info') || lowerText.includes('status')) {
            return {
                intent: 'system_info',
                confidence: 0.9,
                entities: { action: 'info', type: 'system' },
                response: 'Here is the system information.'
            };
        } else if (lowerText.includes('help') || lowerText.includes('assist')) {
            return {
                intent: 'system_help',
                confidence: 0.8,
                entities: { action: 'help', type: 'system' },
                response: 'I can help you with tasks, reminders, and system information. What would you like to do?'
            };
        } else if (lowerText.includes('time') || lowerText.includes('date')) {
            return {
                intent: 'system_time',
                confidence: 0.8,
                entities: { action: 'time', type: 'system' },
                response: `The current time is ${new Date().toLocaleString()}.`
            };
        }
        
        return {
            intent: 'system_unknown',
            confidence: 0.5,
            entities: { type: 'system' },
            response: 'I understand you want system information, but I need more specific instructions.'
        };
    }
} 