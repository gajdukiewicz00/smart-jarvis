import { NLPRepository } from '../../domain/repositories/NLPRepository';
import { NLPRequest } from '../../domain/entities/NLPRequest';
import { NLPResponse } from '../../domain/entities/NLPResponse';
import { Intent } from '../../domain/entities/Intent';

export class InMemoryNLPRepository implements NLPRepository {
  private requests: Map<string, NLPRequest> = new Map();
  private responses: Map<string, NLPResponse> = new Map();
  private intents: Map<string, Intent> = new Map();

  // Request operations
  async saveRequest(request: NLPRequest): Promise<NLPRequest> {
    this.requests.set(request.id, request);
    return request;
  }

  async getRequestById(id: string): Promise<NLPRequest | null> {
    return this.requests.get(id) || null;
  }

  async getAllRequests(): Promise<NLPRequest[]> {
    return Array.from(this.requests.values());
  }

  async getRequestsByDateRange(startDate: Date, endDate: Date): Promise<NLPRequest[]> {
    return Array.from(this.requests.values()).filter(request => {
      const createdAt = request.createdAt;
      return createdAt >= startDate && createdAt <= endDate;
    });
  }

  // Response operations
  async saveResponse(response: NLPResponse): Promise<NLPResponse> {
    this.responses.set(response.id, response);
    return response;
  }

  async getResponseById(id: string): Promise<NLPResponse | null> {
    return this.responses.get(id) || null;
  }

  async getResponsesByRequestId(requestId: string): Promise<NLPResponse[]> {
    return Array.from(this.responses.values()).filter(
      response => response.requestId === requestId
    );
  }

  async getAllResponses(): Promise<NLPResponse[]> {
    return Array.from(this.responses.values());
  }

  // Intent operations
  async saveIntent(intent: Intent): Promise<Intent> {
    this.intents.set(intent.id, intent);
    return intent;
  }

  async getIntentById(id: string): Promise<Intent | null> {
    return this.intents.get(id) || null;
  }

  async getIntentsByType(type: string): Promise<Intent[]> {
    return Array.from(this.intents.values()).filter(
      intent => intent.type.getValue() === type
    );
  }

  async getAllIntents(): Promise<Intent[]> {
    return Array.from(this.intents.values());
  }

  // Statistics operations
  async getProcessingStatistics(): Promise<{
    totalRequests: number;
    totalResponses: number;
    successfulRequests: number;
    failedRequests: number;
    averageProcessingTime: number;
    requestsByType: Record<string, number>;
    responsesByStatus: Record<string, number>;
  }> {
    const requests = Array.from(this.requests.values());
    const responses = Array.from(this.responses.values());
    const intents = Array.from(this.intents.values());

    // Calculate request types
    const requestsByType: Record<string, number> = {};
    requests.forEach(request => {
      const type = request.shouldExecute() ? 'executable' : 'query';
      requestsByType[type] = (requestsByType[type] || 0) + 1;
    });

    // Calculate response statuses
    const responsesByStatus: Record<string, number> = {};
    responses.forEach(response => {
      const status = response.status.getValue();
      responsesByStatus[status] = (responsesByStatus[status] || 0) + 1;
    });

    // Calculate processing times
    const processingTimes = responses.map(r => r.processingTime);
    const averageProcessingTime = processingTimes.length > 0 
      ? processingTimes.reduce((sum, time) => sum + time, 0) / processingTimes.length 
      : 0;

    return {
      totalRequests: requests.length,
      totalResponses: responses.length,
      successfulRequests: responses.filter(r => r.isSuccessful()).length,
      failedRequests: responses.filter(r => r.isFailed()).length,
      averageProcessingTime,
      requestsByType,
      responsesByStatus
    };
  }

  // Cleanup operations
  async deleteOldRequests(olderThan: Date): Promise<number> {
    let deletedCount = 0;
    for (const [id, request] of this.requests.entries()) {
      if (request.createdAt < olderThan) {
        this.requests.delete(id);
        deletedCount++;
      }
    }
    return deletedCount;
  }

  async deleteOldResponses(olderThan: Date): Promise<number> {
    let deletedCount = 0;
    for (const [id, response] of this.responses.entries()) {
      if (response.createdAt < olderThan) {
        this.responses.delete(id);
        deletedCount++;
      }
    }
    return deletedCount;
  }

  async deleteOldIntents(olderThan: Date): Promise<number> {
    let deletedCount = 0;
    for (const [id, intent] of this.intents.entries()) {
      if (intent.createdAt < olderThan) {
        this.intents.delete(id);
        deletedCount++;
      }
    }
    return deletedCount;
  }

  // Utility methods for testing
  clear(): void {
    this.requests.clear();
    this.responses.clear();
    this.intents.clear();
  }

  getRequestCount(): number {
    return this.requests.size;
  }

  getResponseCount(): number {
    return this.responses.size;
  }

  getIntentCount(): number {
    return this.intents.size;
  }
} 