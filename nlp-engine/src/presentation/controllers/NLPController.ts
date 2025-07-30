import { Request, Response } from 'express';
import { ProcessIntentUseCase } from '../../application/usecases/ProcessIntentUseCase';
import { ExecuteActionUseCase } from '../../application/usecases/ExecuteActionUseCase';
import { ProcessIntentRequestDto } from '../../application/dto/NLPRequestDto';
import { ExecuteActionRequestDto } from '../../application/dto/NLPRequestDto';
import { HealthResponseDto, StatisticsResponseDto, IntentListResponseDto, ExamplesResponseDto } from '../../application/dto/NLPResponseDto';

export class NLPController {
    constructor(
        private processIntentUseCase: ProcessIntentUseCase,
        private executeActionUseCase: ExecuteActionUseCase
    ) {}

    async processIntent(req: Request, res: Response): Promise<void> {
        try {
            const requestDto: ProcessIntentRequestDto = {
                text: req.body.text,
                context: req.body.context,
                execute: req.body.execute
            };

            if (!requestDto.text) {
                res.status(400).json({
                    success: false,
                    error: 'Text is required'
                });
                return;
            }

            const result = await this.processIntentUseCase.execute(requestDto);
            res.status(result.success ? 200 : 400).json(result);

        } catch (error) {
            console.error('Error processing intent:', error);
            res.status(500).json({
                success: false,
                error: 'Internal server error'
            });
        }
    }

    async executeAction(req: Request, res: Response): Promise<void> {
        try {
            const requestDto: ExecuteActionRequestDto = {
                intent: req.body.intent,
                entities: req.body.entities || {}
            };

            if (!requestDto.intent) {
                res.status(400).json({
                    success: false,
                    error: 'Intent is required'
                });
                return;
            }

            const result = await this.executeActionUseCase.execute(requestDto);
            res.status(result.success ? 200 : 400).json(result);

        } catch (error) {
            console.error('Error executing action:', error);
            res.status(500).json({
                success: false,
                error: 'Internal server error'
            });
        }
    }

    async getHealth(req: Request, res: Response): Promise<void> {
        try {
            const healthResponse: HealthResponseDto = {
                status: 'healthy',
                service: 'nlp-engine',
                timestamp: new Date().toISOString(),
                uptime: process.uptime(),
                details: {
                    version: '1.0.0',
                    environment: process.env['NODE_ENV'] || 'development'
                }
            };

            res.status(200).json(healthResponse);

        } catch (error) {
            console.error('Error getting health:', error);
            res.status(500).json({
                status: 'unhealthy',
                service: 'nlp-engine',
                timestamp: new Date().toISOString(),
                uptime: process.uptime(),
                error: 'Health check failed'
            });
        }
    }

    async getStatistics(req: Request, res: Response): Promise<void> {
        try {
            // Mock statistics for now
            const statisticsResponse: StatisticsResponseDto = {
                success: true,
                data: {
                    totalRequests: 0,
                    totalResponses: 0,
                    successfulRequests: 0,
                    failedRequests: 0,
                    averageProcessingTime: 0,
                    requestsByType: {},
                    responsesByStatus: {},
                    intentAccuracy: {}
                }
            };

            res.status(200).json(statisticsResponse);

        } catch (error) {
            console.error('Error getting statistics:', error);
            res.status(500).json({
                success: false,
                error: 'Failed to get statistics'
            });
        }
    }

    async getIntents(req: Request, res: Response): Promise<void> {
        try {
            const intentsResponse: IntentListResponseDto = {
                success: true,
                data: {
                    intents: [
                        'task_create',
                        'task_list',
                        'task_complete',
                        'task_delete',
                        'task_update',
                        'unknown'
                    ],
                    total: 6
                }
            };

            res.status(200).json(intentsResponse);

        } catch (error) {
            console.error('Error getting intents:', error);
            res.status(500).json({
                success: false,
                error: 'Failed to get intents'
            });
        }
    }

    async getExamples(req: Request, res: Response): Promise<void> {
        try {
            const examplesResponse: ExamplesResponseDto = {
                success: true,
                data: {
                    task_management: [
                        'Create a task called "Buy groceries"',
                        'Add a high priority task "Finish project report"',
                        'Show my tasks',
                        'List pending tasks',
                        'Complete task "Buy groceries"',
                        'Update task priority to urgent',
                        'Delete task "Old task"'
                    ],
                    natural_language: [
                        'I need to buy groceries',
                        'Remind me to call the doctor',
                        'What tasks are due today?',
                        'Mark the project task as done',
                        'Set priority to high for the meeting task',
                        'Show me all my pending work'
                    ]
                }
            };

            res.status(200).json(examplesResponse);

        } catch (error) {
            console.error('Error getting examples:', error);
            res.status(500).json({
                success: false,
                error: 'Failed to get examples'
            });
        }
    }
} 