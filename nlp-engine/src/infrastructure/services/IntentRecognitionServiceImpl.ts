import { IntentRecognitionService } from '../../domain/services/IntentRecognitionService';
import { Intent } from '../../domain/entities/Intent';
import { NLPRequest } from '../../domain/entities/NLPRequest';
import { NLPResponse } from '../../domain/entities/NLPResponse';
import { IntentType } from '../../domain/value-objects/IntentType';

export class IntentRecognitionServiceImpl implements IntentRecognitionService {
  private readonly taskPatterns = {
    create: /(?:create|add|new)\s+(?:a\s+)?task\s+(?:called\s+)?['"]([^'"]+)['"]/i,
    list: /(?:list|show|get|display)\s+(?:my\s+)?tasks?/i,
    complete: /(?:complete|finish|done|mark\s+as\s+done)\s+(?:task\s+)?['"]([^'"]+)['"]/i,
    delete: /(?:delete|remove)\s+(?:task\s+)?['"]([^'"]+)['"]/i,
    statistics: /(?:statistics|stats|summary)/i
  };

  async recognizeIntent(text: string, context?: Record<string, any>): Promise<Intent> {
    const normalizedText = text.toLowerCase().trim();
    
    // Check task patterns
    for (const [action, pattern] of Object.entries(this.taskPatterns)) {
      const match = normalizedText.match(pattern);
      if (match) {
        const entities = this.extractTaskEntities(action, match);
        return Intent.create(
          this.getTaskIntentType(action),
          0.9,
          entities,
          text,
          context
        );
      }
    }

    // Natural language processing for task-related commands
    if (this.isNaturalLanguageTask(normalizedText)) {
      const entities = this.extractNaturalLanguageEntities(normalizedText);
      return Intent.create(
        IntentType.TASK_CREATE,
        0.6,
        entities,
        text,
        context
      );
    }

    // Unknown intent
    return Intent.createUnknown(text, context);
  }

  async processRequest(request: NLPRequest): Promise<NLPResponse> {
    const startTime = Date.now();

    try {
      const intent = await this.recognizeIntent(request.text, request.context);
      const response = this.generateResponse(intent);
      const processingTime = Date.now() - startTime;

      return NLPResponse.create(
        request.id,
        intent,
        response,
        processingTime
      );
    } catch (error) {
      const processingTime = Date.now() - startTime;
      const unknownIntent = Intent.createUnknown(request.text, request.context);
      
      return NLPResponse.createFailed(
        request.id,
        unknownIntent,
        error instanceof Error ? error.message : 'Processing failed',
        processingTime
      );
    }
  }

  async executeAction(intent: Intent, entities: Record<string, any>): Promise<{
    success: boolean;
    result: any;
    message: string;
  }> {
    return {
      success: true,
      result: { intent: intent.type.getValue(), entities },
      message: `Action executed: ${intent.type.getValue()}`
    };
  }

  async validateText(text: string): Promise<{
    isValid: boolean;
    errors: string[];
  }> {
    const errors: string[] = [];

    if (!text || text.trim().length === 0) {
      errors.push('Text cannot be empty');
    }

    if (text.length > 1000) {
      errors.push('Text is too long (max 1000 characters)');
    }

    return {
      isValid: errors.length === 0,
      errors
    };
  }

  async getServiceHealth(): Promise<{
    status: 'healthy' | 'degraded' | 'unhealthy';
    details: Record<string, any>;
  }> {
    return {
      status: 'healthy',
      details: {
        patterns: Object.keys(this.taskPatterns).length,
        timestamp: new Date().toISOString()
      }
    };
  }

  async getProcessingStatistics(): Promise<{
    totalProcessed: number;
    successfulRequests: number;
    failedRequests: number;
    averageProcessingTime: number;
    intentAccuracy: Record<string, number>;
  }> {
    return {
      totalProcessed: 0,
      successfulRequests: 0,
      failedRequests: 0,
      averageProcessingTime: 0,
      intentAccuracy: {}
    };
  }

  private getTaskIntentType(action: string): IntentType {
    switch (action) {
      case 'create': return IntentType.TASK_CREATE;
      case 'list': return IntentType.TASK_LIST;
      case 'complete': return IntentType.TASK_COMPLETE;
      case 'delete': return IntentType.TASK_DELETE;
      case 'statistics': return IntentType.TASK_STATISTICS;
      default: return IntentType.UNKNOWN;
    }
  }

  private extractTaskEntities(action: string, match: RegExpMatchArray): Record<string, any> {
    const entities: Record<string, any> = { action };

    if (action === 'create' && match[1]) {
      entities['taskDetails'] = {
        title: match[1],
        priority: 'MEDIUM',
        description: `Created via voice command`
      };
    } else if ((action === 'complete' || action === 'delete') && match[1]) {
      entities['taskTitle'] = match[1];
    }

    return entities;
  }

  private isNaturalLanguageTask(text: string): boolean {
    const taskKeywords = ['task', 'todo', 'remind', 'need to', 'have to', 'should'];
    return taskKeywords.some(keyword => text.includes(keyword));
  }

  private extractNaturalLanguageEntities(text: string): Record<string, any> {
    return {
      action: 'create',
      taskDetails: {
        title: text,
        priority: 'MEDIUM',
        description: `Natural language task: "${text}"`
      }
    };
  }

  private generateResponse(intent: Intent): string {
    if (intent.isUnknown()) {
      return "I'm sorry, I didn't understand that command. Could you please rephrase it?";
    }

    if (intent.isTaskIntent()) {
      const taskDetails = intent.getTaskDetails();
      
      switch (intent.type.getValue()) {
        case IntentType.TASK_CREATE:
          return `I'll create a task for you: "${taskDetails?.['title'] || 'Untitled task'}"`;
        case IntentType.TASK_LIST:
          return "I'll show you your tasks.";
        case IntentType.TASK_COMPLETE:
          return `I'll mark the task "${taskDetails?.['title'] || 'Unknown task'}" as completed.`;
        case IntentType.TASK_DELETE:
          return `I'll delete the task "${taskDetails?.['title'] || 'Unknown task'}".`;
        case IntentType.TASK_STATISTICS:
          return "I'll show you your task statistics.";
        default:
          return "I'll process your task request.";
      }
    }

    return "I understand your request and will process it accordingly.";
  }
} 