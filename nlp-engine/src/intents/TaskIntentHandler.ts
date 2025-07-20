import { IntentHandler, IntentResult } from './IntentHandler';

export class TaskIntentHandler implements IntentHandler {
    
    async handle(text: string, context?: any): Promise<IntentResult> {
        const lowerText = text.toLowerCase();
        
        if (lowerText.includes('create') || lowerText.includes('add') || lowerText.includes('new')) {
            return {
                intent: 'task_create',
                confidence: 0.9,
                entities: { action: 'create', type: 'task' },
                response: 'I will create a new task for you.'
            };
        } else if (lowerText.includes('list') || lowerText.includes('show') || lowerText.includes('get')) {
            return {
                intent: 'task_list',
                confidence: 0.8,
                entities: { action: 'list', type: 'task' },
                response: 'Here are your tasks.'
            };
        } else if (lowerText.includes('delete') || lowerText.includes('remove')) {
            return {
                intent: 'task_delete',
                confidence: 0.8,
                entities: { action: 'delete', type: 'task' },
                response: 'I will delete the specified task.'
            };
        } else if (lowerText.includes('update') || lowerText.includes('modify')) {
            return {
                intent: 'task_update',
                confidence: 0.8,
                entities: { action: 'update', type: 'task' },
                response: 'I will update the task for you.'
            };
        }
        
        return {
            intent: 'task_unknown',
            confidence: 0.5,
            entities: { type: 'task' },
            response: 'I understand you want to work with tasks, but I need more specific instructions.'
        };
    }
} 