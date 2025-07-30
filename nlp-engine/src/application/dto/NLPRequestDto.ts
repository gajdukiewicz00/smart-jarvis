export interface NLPRequestDto {
  text: string;
  context?: Record<string, any>;
  execute?: boolean;
}

export interface NLPRequestResponseDto {
  id: string;
  text: string;
  context?: Record<string, any>;
  execute: boolean;
  createdAt: string;
}

export interface ProcessIntentRequestDto {
  text: string;
  context?: Record<string, any>;
  execute?: boolean;
}

export interface ExecuteActionRequestDto {
  intent: string;
  entities: Record<string, any>;
}

export interface HealthCheckRequestDto {
  includeDetails?: boolean;
}

export interface StatisticsRequestDto {
  startDate?: string;
  endDate?: string;
  includeDetails?: boolean;
} 