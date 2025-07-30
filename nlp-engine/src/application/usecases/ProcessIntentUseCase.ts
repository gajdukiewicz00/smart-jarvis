import { NLPRepository } from '../../domain/repositories/NLPRepository';
import { IntentRecognitionService } from '../../domain/services/IntentRecognitionService';
import { ProcessIntentRequestDto } from '../dto/NLPRequestDto';
import { ProcessIntentResponseDto, IntentDto } from '../dto/NLPResponseDto';

export class ProcessIntentUseCase {
    constructor(
        private nlpRepository: NLPRepository,
        private intentRecognitionService: IntentRecognitionService
    ) {}

    async execute(request: ProcessIntentRequestDto): Promise<ProcessIntentResponseDto> {
        const startTime = Date.now();

        try {
            // Process intent using recognition service
            const intent = await this.intentRecognitionService.recognizeIntent(
                request.text,
                request.context || {}
            );

            // Save intent to repository
            await this.nlpRepository.saveIntent(intent);

            // Create response DTO
            const intentDto: IntentDto = {
                id: intent.id,
                type: intent.type.getValue(),
                confidence: intent.confidence.getValue(),
                entities: intent.entities,
                text: intent.text,
                context: intent.context || {},
                createdAt: intent.createdAt.toISOString()
            };

            const processingTime = Date.now() - startTime;

            return {
                success: true,
                data: {
                    intent: intentDto,
                    response: 'Intent processed successfully',
                    processingTime
                }
            };

        } catch (error) {
            const processingTime = Date.now() - startTime;
            
            return {
                success: false,
                data: {
                    intent: {
                        id: '',
                        type: 'unknown',
                        confidence: 0,
                        entities: {},
                        text: request.text,
                        context: request.context || {},
                        createdAt: new Date().toISOString()
                    },
                    response: 'Internal server error',
                    processingTime
                },
                error: error instanceof Error ? error.message : 'Unknown error occurred'
            };
        }
    }
} 