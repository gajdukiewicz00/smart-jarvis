import { IntentRecognitionService } from '../../domain/services/IntentRecognitionService';
import { NLPRepository } from '../../domain/repositories/NLPRepository';
import { Intent } from '../../domain/entities/Intent';
import { ExecuteActionRequestDto } from '../dto/NLPRequestDto';
import { ExecuteActionResponseDto } from '../dto/NLPResponseDto';

export class ExecuteActionUseCase {
  constructor(
    private readonly intentRecognitionService: IntentRecognitionService,
    private readonly nlpRepository: NLPRepository
  ) {}

  async execute(requestDto: ExecuteActionRequestDto): Promise<ExecuteActionResponseDto> {
    const startTime = Date.now();

    try {
      // Create intent from request
      const intent = Intent.create(
        requestDto.intent as any, // Assuming intent is a valid IntentType
        0.9, // High confidence for explicit intent execution
        requestDto.entities,
        'Executed via API',
        { source: 'api_execute' }
      );

      // Save intent
      const savedIntent = await this.nlpRepository.saveIntent(intent);

      // Execute action
      const result = await this.intentRecognitionService.executeAction(savedIntent, requestDto.entities);

      const processingTime = Date.now() - startTime;

      return {
        success: result.success,
        data: {
          result: result.result,
          message: result.message,
          processingTime
        },
        ...(result.success ? {} : { error: result.message })
      };

    } catch (error) {
      const processingTime = Date.now() - startTime;
      
      return {
        success: false,
        data: {
          result: null,
          message: 'Failed to execute action',
          processingTime
        },
        error: error instanceof Error ? error.message : 'Unknown error'
      };
    }
  }
} 