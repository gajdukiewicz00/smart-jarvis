import { IntentProcessor } from './IntentProcessor';
import { TaskServiceClient } from './TaskServiceClient';

// Simple cache for NLP results
const nlpCache = new Map<string, any>();
const CACHE_TTL = 5 * 60 * 1000; // 5 minutes

export interface IntentResult {
    intent: string;
    confidence: number;
    entities: any;
    response?: string;
}

export class NLPService {
    private intentProcessor: IntentProcessor;
    private taskServiceClient: TaskServiceClient;

    constructor(intentProcessor: IntentProcessor, taskServiceUrl: string) {
        this.intentProcessor = intentProcessor;
        this.taskServiceClient = new TaskServiceClient(taskServiceUrl);
    }

    async processIntent(text: string, context?: any): Promise<IntentResult> {
        // Check cache first
        const cacheKey = `${text.toLowerCase()}_${JSON.stringify(context || {})}`;
        const cached = nlpCache.get(cacheKey);
        if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
            return cached.result;
        }

        try {
            const result = await this.intentProcessor.processIntent(text, context);
            
            // Cache the result
            nlpCache.set(cacheKey, {
                result,
                timestamp: Date.now()
            });

            // Clean up old cache entries
            this.cleanupCache();

            return result;
        } catch (error) {
            console.error('Error processing intent:', error);
            return {
                intent: 'error',
                confidence: 0,
                entities: { error: error instanceof Error ? error.message : 'Unknown error' },
                response: 'Sorry, I encountered an error processing your request.'
            };
        }
    }

    private cleanupCache() {
        const now = Date.now();
        for (const [key, value] of nlpCache.entries()) {
            if (now - value.timestamp > CACHE_TTL) {
                nlpCache.delete(key);
            }
        }
    }

    private async enhanceResponse(result: IntentResult, originalText: string): Promise<IntentResult> {
        const lowerText = originalText.toLowerCase();

        // Enhance task-related responses with actual data
        if (result.intent.startsWith('task_')) {
            return await this.enhanceTaskResponse(result, lowerText);
        }

        return result;
    }

    private async enhanceTaskResponse(result: IntentResult, lowerText: string): Promise<IntentResult> {
        try {
            switch (result.intent) {
                case 'task_list':
                    const tasks = await this.taskServiceClient.getAllTasks();
                    const stats = await this.taskServiceClient.getTaskStatistics();
                    
                    if (tasks.length === 0) {
                        return {
                            ...result,
                            response: 'You have no tasks at the moment. Would you like to create one?',
                            entities: {
                                ...result.entities,
                                taskCount: 0,
                                statistics: stats
                            }
                        };
                    }

                    const taskList = tasks.map(task => 
                        `- ${task.title} (${task.status}, ${task.priority})`
                    ).join('\n');

                    return {
                        ...result,
                        response: `You have ${tasks.length} tasks:\n${taskList}`,
                        entities: {
                            ...result.entities,
                            tasks: tasks,
                            taskCount: tasks.length,
                            statistics: stats
                        }
                    };

                case 'task_statistics':
                    const statistics = await this.taskServiceClient.getTaskStatistics();
                    return {
                        ...result,
                        response: `Task Statistics:\n- Total: ${statistics.totalTasks}\n- Pending: ${statistics.pendingTasks}\n- In Progress: ${statistics.inProgressTasks}\n- Completed: ${statistics.completedTasks}\n- Overdue: ${statistics.overdueTasks}`,
                        entities: {
                            ...result.entities,
                            statistics: statistics
                        }
                    };

                case 'task_create':
                    // Extract task details from text
                    const taskDetails = this.extractTaskDetails(lowerText);
                    return {
                        ...result,
                        response: `I'll create a task "${taskDetails.title}" with ${taskDetails.priority} priority.`,
                        entities: {
                            ...result.entities,
                            taskDetails: taskDetails
                        }
                    };

                case 'task_complete':
                    // Try to find task by title or ID
                    const taskToComplete = this.extractTaskIdentifier(lowerText);
                    return {
                        ...result,
                        response: `I'll mark the task as completed.`,
                        entities: {
                            ...result.entities,
                            taskIdentifier: taskToComplete
                        }
                    };

                default:
                    return result;
            }
        } catch (error) {
            return {
                ...result,
                response: `I encountered an error while processing your request: ${error instanceof Error ? error.message : 'Unknown error'}`,
                entities: {
                    ...result.entities,
                    error: error instanceof Error ? error.message : 'Unknown error'
                }
            };
        }
    }

    private extractTaskDetails(text: string): { title: string; priority: string; description?: string; category?: string } {
        // Simple extraction logic - can be enhanced with NLP libraries
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

        // Extract title (simplified - in real implementation would use NLP)
        const titleMatch = text.match(/(?:create|add|new)\s+(?:task\s+)?["']?([^"']+)["']?/i);
        const title = titleMatch?.[1] || 'New Task';

        return {
            title: title,
            priority: priority,
            description: 'Created via voice command'
        };
    }

    private extractTaskIdentifier(text: string): string | null {
        // Try to extract task ID or title
        const idMatch = text.match(/(?:task|id)\s+([a-f0-9-]+)/i);
        if (idMatch && idMatch[1]) {
            return idMatch[1];
        }

        const titleMatch = text.match(/(?:complete|finish|done)\s+(?:task\s+)?["']?([^"']+)["']?/i);
        if (titleMatch && titleMatch[1]) {
            return titleMatch[1];
        }

        return null;
    }

    async executeTaskAction(intent: string, entities: any): Promise<IntentResult> {
        try {
            switch (intent) {
                case 'task_create':
                    if (entities.taskDetails) {
                        const task = await this.taskServiceClient.createTask({
                            title: entities.taskDetails.title,
                            description: entities.taskDetails.description || 'Created via voice command',
                            priority: entities.taskDetails.priority,
                            status: 'PENDING',
                            category: entities.taskDetails.category
                        });
                        
                        return {
                            intent: 'task_created',
                            confidence: 1.0,
                            entities: { task },
                            response: `Task "${task.title}" has been created successfully!`
                        };
                    }
                    break;

                case 'task_complete':
                    if (entities.taskIdentifier) {
                        // This would need more sophisticated task matching
                        return {
                            intent: 'task_completed',
                            confidence: 0.8,
                            entities: { taskIdentifier: entities.taskIdentifier },
                            response: `I'll help you complete that task. Please specify which task you'd like to complete.`
                        };
                    }
                    break;
            }

            return {
                intent: 'action_failed',
                confidence: 0.5,
                entities: { originalIntent: intent },
                response: 'I couldn\'t execute that action. Please try again with more specific details.'
            };
        } catch (error) {
            return {
                intent: 'action_error',
                confidence: 1.0,
                entities: { error: error instanceof Error ? error.message : 'Unknown error' },
                response: `Error executing action: ${error instanceof Error ? error.message : 'Unknown error'}`
            };
        }
    }
} 