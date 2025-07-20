import { IntentHandler, IntentResult } from './IntentHandler';

export class TaskIntentHandler implements IntentHandler {
    
    async handle(text: string, context?: any): Promise<IntentResult> {
        const lowerText = text.toLowerCase();
        
        // Enhanced intent recognition with more natural language patterns
        if (this.matchesCreatePattern(lowerText)) {
            return {
                intent: 'task_create',
                confidence: 0.95,
                entities: { 
                    action: 'create', 
                    type: 'task',
                    extractedDetails: this.extractTaskDetails(lowerText)
                },
                response: 'I will create a new task for you.'
            };
        } 
        
        if (this.matchesListPattern(lowerText)) {
            return {
                intent: 'task_list',
                confidence: 0.9,
                entities: { 
                    action: 'list', 
                    type: 'task',
                    filter: this.extractListFilter(lowerText)
                },
                response: 'Here are your tasks.'
            };
        } 
        
        if (this.matchesStatisticsPattern(lowerText)) {
            return {
                intent: 'task_statistics',
                confidence: 0.9,
                entities: { 
                    action: 'statistics', 
                    type: 'task'
                },
                response: 'Here are your task statistics.'
            };
        }
        
        if (this.matchesCompletePattern(lowerText)) {
            return {
                intent: 'task_complete',
                confidence: 0.85,
                entities: { 
                    action: 'complete', 
                    type: 'task',
                    taskIdentifier: this.extractTaskIdentifier(lowerText)
                },
                response: 'I will mark the task as completed.'
            };
        }
        
        if (this.matchesUpdatePattern(lowerText)) {
            return {
                intent: 'task_update',
                confidence: 0.8,
                entities: { 
                    action: 'update', 
                    type: 'task',
                    taskIdentifier: this.extractTaskIdentifier(lowerText),
                    updates: this.extractUpdateDetails(lowerText)
                },
                response: 'I will update the task for you.'
            };
        }
        
        if (this.matchesDeletePattern(lowerText)) {
            return {
                intent: 'task_delete',
                confidence: 0.8,
                entities: { 
                    action: 'delete', 
                    type: 'task',
                    taskIdentifier: this.extractTaskIdentifier(lowerText)
                },
                response: 'I will delete the specified task.'
            };
        }
        
        if (this.matchesPriorityPattern(lowerText)) {
            return {
                intent: 'task_priority',
                confidence: 0.85,
                entities: { 
                    action: 'priority', 
                    type: 'task',
                    priority: this.extractPriority(lowerText),
                    taskIdentifier: this.extractTaskIdentifier(lowerText)
                },
                response: 'I will update the task priority.'
            };
        }
        
        if (this.matchesStatusPattern(lowerText)) {
            return {
                intent: 'task_status',
                confidence: 0.85,
                entities: { 
                    action: 'status', 
                    type: 'task',
                    status: this.extractStatus(lowerText),
                    taskIdentifier: this.extractTaskIdentifier(lowerText)
                },
                response: 'I will update the task status.'
            };
        }
        
        return {
            intent: 'task_unknown',
            confidence: 0.5,
            entities: { type: 'task' },
            response: 'I understand you want to work with tasks, but I need more specific instructions. You can say things like "create a task", "show my tasks", "complete task", etc.'
        };
    }

    private matchesCreatePattern(text: string): boolean {
        const createPatterns = [
            /create.*task/i,
            /add.*task/i,
            /new.*task/i,
            /make.*task/i,
            /set.*task/i,
            /create.*todo/i,
            /add.*todo/i
        ];
        return createPatterns.some(pattern => pattern.test(text));
    }

    private matchesListPattern(text: string): boolean {
        const listPatterns = [
            /list.*task/i,
            /show.*task/i,
            /get.*task/i,
            /display.*task/i,
            /what.*task/i,
            /my.*task/i,
            /all.*task/i,
            /pending.*task/i,
            /completed.*task/i
        ];
        return listPatterns.some(pattern => pattern.test(text));
    }

    private matchesStatisticsPattern(text: string): boolean {
        const statsPatterns = [
            /statistics/i,
            /stats/i,
            /summary/i,
            /overview/i,
            /how.*many.*task/i,
            /task.*count/i,
            /task.*summary/i
        ];
        return statsPatterns.some(pattern => pattern.test(text));
    }

    private matchesCompletePattern(text: string): boolean {
        const completePatterns = [
            /complete.*task/i,
            /finish.*task/i,
            /done.*task/i,
            /mark.*complete/i,
            /task.*done/i,
            /task.*finished/i
        ];
        return completePatterns.some(pattern => pattern.test(text));
    }

    private matchesUpdatePattern(text: string): boolean {
        const updatePatterns = [
            /update.*task/i,
            /modify.*task/i,
            /change.*task/i,
            /edit.*task/i,
            /modify.*task/i
        ];
        return updatePatterns.some(pattern => pattern.test(text));
    }

    private matchesDeletePattern(text: string): boolean {
        const deletePatterns = [
            /delete.*task/i,
            /remove.*task/i,
            /cancel.*task/i,
            /drop.*task/i
        ];
        return deletePatterns.some(pattern => pattern.test(text));
    }

    private matchesPriorityPattern(text: string): boolean {
        const priorityPatterns = [
            /priority.*high/i,
            /priority.*low/i,
            /priority.*medium/i,
            /priority.*urgent/i,
            /high.*priority/i,
            /low.*priority/i,
            /urgent.*priority/i
        ];
        return priorityPatterns.some(pattern => pattern.test(text));
    }

    private matchesStatusPattern(text: string): boolean {
        const statusPatterns = [
            /status.*pending/i,
            /status.*progress/i,
            /status.*completed/i,
            /status.*cancelled/i,
            /pending.*status/i,
            /in.*progress/i,
            /completed.*status/i
        ];
        return statusPatterns.some(pattern => pattern.test(text));
    }

    private extractTaskDetails(text: string): any {
        const priorityKeywords = {
            'urgent': 'URGENT',
            'high': 'HIGH',
            'medium': 'MEDIUM',
            'low': 'LOW'
        };

        let priority = 'MEDIUM';
        for (const [keyword, value] of Object.entries(priorityKeywords)) {
            if (text.includes(keyword)) {
                priority = value;
                break;
            }
        }

        // Extract title from various patterns
        const titlePatterns = [
            /(?:create|add|new|make)\s+(?:a\s+)?(?:task\s+)?["']?([^"']+)["']?/i,
            /(?:task|todo)\s+["']?([^"']+)["']?/i,
            /["']([^"']+)["']\s+(?:as\s+)?(?:a\s+)?task/i
        ];

        let title = 'New Task';
        for (const pattern of titlePatterns) {
            const match = text.match(pattern);
            if (match?.[1]) {
                title = match[1].trim();
                break;
            }
        }

        return {
            title: title,
            priority: priority,
            description: 'Created via voice command'
        };
    }

    private extractListFilter(text: string): string {
        if (text.includes('pending') || text.includes('not started')) return 'PENDING';
        if (text.includes('progress') || text.includes('in progress')) return 'IN_PROGRESS';
        if (text.includes('completed') || text.includes('done') || text.includes('finished')) return 'COMPLETED';
        if (text.includes('cancelled') || text.includes('cancelled')) return 'CANCELLED';
        if (text.includes('urgent') || text.includes('high priority')) return 'URGENT';
        return 'ALL';
    }

    private extractTaskIdentifier(text: string): string | null {
        // Try to extract task ID
        const idMatch = text.match(/(?:task|id)\s+([a-f0-9-]+)/i);
        if (idMatch?.[1]) {
            return idMatch[1];
        }

        // Try to extract task title
        const titlePatterns = [
            /(?:complete|finish|done|update|delete|remove)\s+(?:task\s+)?["']?([^"']+)["']?/i,
            /(?:task|todo)\s+["']?([^"']+)["']?\s+(?:as\s+)?(?:complete|done|finished)/i
        ];

        for (const pattern of titlePatterns) {
            const match = text.match(pattern);
            if (match?.[1]) {
                return match[1].trim();
            }
        }

        return null;
    }

    private extractUpdateDetails(text: string): any {
        const updates: any = {};
        
        // Extract priority
        const priorityKeywords = {
            'urgent': 'URGENT',
            'high': 'HIGH',
            'medium': 'MEDIUM',
            'low': 'LOW'
        };

        for (const [keyword, value] of Object.entries(priorityKeywords)) {
            if (text.includes(keyword)) {
                updates.priority = value;
                break;
            }
        }

        // Extract status
        const statusKeywords = {
            'pending': 'PENDING',
            'progress': 'IN_PROGRESS',
            'completed': 'COMPLETED',
            'cancelled': 'CANCELLED'
        };

        for (const [keyword, value] of Object.entries(statusKeywords)) {
            if (text.includes(keyword)) {
                updates.status = value;
                break;
            }
        }

        return updates;
    }

    private extractPriority(text: string): string {
        if (text.includes('urgent')) return 'URGENT';
        if (text.includes('high')) return 'HIGH';
        if (text.includes('medium')) return 'MEDIUM';
        if (text.includes('low')) return 'LOW';
        return 'MEDIUM';
    }

    private extractStatus(text: string): string {
        if (text.includes('pending')) return 'PENDING';
        if (text.includes('progress')) return 'IN_PROGRESS';
        if (text.includes('completed') || text.includes('done')) return 'COMPLETED';
        if (text.includes('cancelled')) return 'CANCELLED';
        return 'PENDING';
    }
} 