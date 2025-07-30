import { NLPRequest } from '../entities/NLPRequest';
import { NLPResponse } from '../entities/NLPResponse';
import { Intent } from '../entities/Intent';

export interface NLPRepository {
  // Request operations
  saveRequest(request: NLPRequest): Promise<NLPRequest>;
  getRequestById(id: string): Promise<NLPRequest | null>;
  getAllRequests(): Promise<NLPRequest[]>;
  getRequestsByDateRange(startDate: Date, endDate: Date): Promise<NLPRequest[]>;
  
  // Response operations
  saveResponse(response: NLPResponse): Promise<NLPResponse>;
  getResponseById(id: string): Promise<NLPResponse | null>;
  getResponsesByRequestId(requestId: string): Promise<NLPResponse[]>;
  getAllResponses(): Promise<NLPResponse[]>;
  
  // Intent operations
  saveIntent(intent: Intent): Promise<Intent>;
  getIntentById(id: string): Promise<Intent | null>;
  getIntentsByType(type: string): Promise<Intent[]>;
  getAllIntents(): Promise<Intent[]>;
  
  // Statistics operations
  getProcessingStatistics(): Promise<{
    totalRequests: number;
    totalResponses: number;
    successfulRequests: number;
    failedRequests: number;
    averageProcessingTime: number;
    requestsByType: Record<string, number>;
    responsesByStatus: Record<string, number>;
  }>;
  
  // Cleanup operations
  deleteOldRequests(olderThan: Date): Promise<number>;
  deleteOldResponses(olderThan: Date): Promise<number>;
  deleteOldIntents(olderThan: Date): Promise<number>;
} 