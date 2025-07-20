import { IntentHandler, IntentResult } from './IntentHandler';

export class ReminderIntentHandler implements IntentHandler {
    
    async handle(text: string, context?: any): Promise<IntentResult> {
        const lowerText = text.toLowerCase();
        
        if (lowerText.includes('create') || lowerText.includes('add') || lowerText.includes('set')) {
            return {
                intent: 'reminder_set',
                confidence: 0.9,
                entities: { action: 'create', type: 'reminder' },
                response: 'I will set a new reminder for you.'
            };
        } else if (lowerText.includes('delete') || lowerText.includes('remove') || lowerText.includes('cancel')) {
            return {
                intent: 'reminder_cancel',
                confidence: 0.8,
                entities: { action: 'delete', type: 'reminder' },
                response: 'I will cancel the specified reminder.'
            };
        } else if (lowerText.includes('list') || lowerText.includes('show')) {
            return {
                intent: 'reminder_list',
                confidence: 0.8,
                entities: { action: 'list', type: 'reminder' },
                response: 'Here are your reminders.'
            };
        }
        
        return {
            intent: 'reminder_unknown',
            confidence: 0.5,
            entities: { type: 'reminder' },
            response: 'I understand you want to work with reminders, but I need more specific instructions.'
        };
    }
} 