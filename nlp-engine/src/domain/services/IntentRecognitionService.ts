import { Intent } from '../entities/Intent';
import { NLPRequest } from '../entities/NLPRequest';
import { NLPResponse } from '../entities/NLPResponse';

export interface IntentRecognitionService {
  // Intent recognition
  recognizeIntent(text: string, context?: Record<string, any>): Promise<Intent>;
  
  // Request processing
  processRequest(request: NLPRequest): Promise<NLPResponse>;
  
  // Action execution
  executeAction(intent: Intent, entities: Record<string, any>): Promise<{
    success: boolean;
    result: any;
    message: string;
  }>;
  
  // Validation
  validateText(text: string): Promise<{
    isValid: boolean;
    errors: string[];
  }>;
  
  // Health check
  getServiceHealth(): Promise<{
    status: 'healthy' | 'degraded' | 'unhealthy';
    details: Record<string, any>;
  }>;
  
  // Statistics
  getProcessingStatistics(): Promise<{
    totalProcessed: number;
    successfulRequests: number;
    failedRequests: number;
    averageProcessingTime: number;
    intentAccuracy: Record<string, number>;
  }>;
} 