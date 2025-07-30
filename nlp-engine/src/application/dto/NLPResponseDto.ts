export interface IntentDto {
  id: string;
  type: string;
  confidence: number;
  entities: Record<string, any>;
  text: string;
  context?: Record<string, any>;
  createdAt: string;
}

export interface NLPResponseDto {
  id: string;
  requestId: string;
  intent: IntentDto;
  response: string;
  status: string;
  processingTime: number;
  createdAt: string;
}

export interface ProcessIntentResponseDto {
  success: boolean;
  data: {
    intent: IntentDto;
    response: string;
    processingTime: number;
  };
  error?: string;
}

export interface ExecuteActionResponseDto {
  success: boolean;
  data: {
    result: any;
    message: string;
    processingTime: number;
  };
  error?: string;
}

export interface HealthResponseDto {
  status: 'healthy' | 'degraded' | 'unhealthy';
  service: string;
  timestamp: string;
  uptime: number;
  details?: Record<string, any>;
}

export interface StatisticsResponseDto {
  success: boolean;
  data: {
    totalRequests: number;
    totalResponses: number;
    successfulRequests: number;
    failedRequests: number;
    averageProcessingTime: number;
    requestsByType: Record<string, number>;
    responsesByStatus: Record<string, number>;
    intentAccuracy: Record<string, number>;
  };
  error?: string;
}

export interface IntentListResponseDto {
  success: boolean;
  data: {
    intents: string[];
    total: number;
  };
  error?: string;
}

export interface ExamplesResponseDto {
  success: boolean;
  data: {
    task_management: string[];
    natural_language: string[];
  };
  error?: string;
} 