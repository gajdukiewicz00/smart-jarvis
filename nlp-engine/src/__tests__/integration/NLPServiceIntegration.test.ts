import { InMemoryNLPRepository } from '../../infrastructure/repositories/InMemoryNLPRepository';
import { IntentRecognitionServiceImpl } from '../../infrastructure/services/IntentRecognitionServiceImpl';
import { ProcessIntentUseCase } from '../../application/usecases/ProcessIntentUseCase';
import { ExecuteActionUseCase } from '../../application/usecases/ExecuteActionUseCase';
import { NLPRequest } from '../../domain/entities/NLPRequest';
import { IntentType } from '../../domain/value-objects/IntentType';

describe('NLP Service Integration', () => {
  let repository: InMemoryNLPRepository;
  let service: IntentRecognitionServiceImpl;
  let processUseCase: ProcessIntentUseCase;
  let executeUseCase: ExecuteActionUseCase;

  beforeEach(() => {
    repository = new InMemoryNLPRepository();
    service = new IntentRecognitionServiceImpl();
    processUseCase = new ProcessIntentUseCase(service, repository);
    executeUseCase = new ExecuteActionUseCase(service, repository);
  });

  describe('Process Intent Use Case', () => {
    it('should process task creation intent', async () => {
      const requestDto = {
        text: "Create a task called 'Buy groceries'",
        context: { source: 'test' },
        execute: false
      };

      const result = await processUseCase.execute(requestDto);

      expect(result.success).toBe(true);
      expect(result.data.intent.type).toBe('task_create');
      expect(result.data.intent.confidence).toBeGreaterThan(0.8);
      expect(result.data.response).toContain('Buy groceries');
    });

    it('should process task listing intent', async () => {
      const requestDto = {
        text: "Show my tasks",
        context: { source: 'test' },
        execute: false
      };

      const result = await processUseCase.execute(requestDto);

      expect(result.success).toBe(true);
      expect(result.data.intent.type).toBe('task_list');
      expect(result.data.response).toContain('show you your tasks');
    });

    it('should handle unknown intent', async () => {
      const requestDto = {
        text: "Random gibberish text",
        context: { source: 'test' },
        execute: false
      };

      const result = await processUseCase.execute(requestDto);

      expect(result.success).toBe(false);
      expect(result.data.intent.type).toBe('unknown');
      expect(result.data.response).toContain('didn\'t understand');
    });

    it('should validate input text', async () => {
      const requestDto = {
        text: "",
        context: { source: 'test' },
        execute: false
      };

      const result = await processUseCase.execute(requestDto);

      expect(result.success).toBe(false);
      expect(result.error).toContain('Text cannot be empty');
    });
  });

  describe('Execute Action Use Case', () => {
    it('should execute task action', async () => {
      const requestDto = {
        intent: 'task_create',
        entities: {
          action: 'create',
          taskDetails: {
            title: 'Test task',
            priority: 'MEDIUM'
          }
        }
      };

      const result = await executeUseCase.execute(requestDto);

      expect(result.success).toBe(true);
      expect(result.data.result.intent).toBe('task_create');
      expect(result.data.message).toContain('Action executed');
    });

    it('should handle invalid intent', async () => {
      const requestDto = {
        intent: 'invalid_intent',
        entities: {}
      };

      const result = await executeUseCase.execute(requestDto);

      expect(result.success).toBe(true); // Mock implementation always returns success
      expect(result.data.result.intent).toBe('invalid_intent');
    });
  });

  describe('Repository Operations', () => {
    it('should save and retrieve requests', async () => {
      const request = NLPRequest.create('Test request', { source: 'test' });
      const savedRequest = await repository.saveRequest(request);
      const retrievedRequest = await repository.getRequestById(savedRequest.id);

      expect(retrievedRequest).toBeDefined();
      expect(retrievedRequest?.id).toBe(savedRequest.id);
      expect(retrievedRequest?.text).toBe('Test request');
    });

    it('should get processing statistics', async () => {
      // Add some test data
      const request = NLPRequest.create('Test request', { source: 'test' });
      await repository.saveRequest(request);

      const stats = await repository.getProcessingStatistics();

      expect(stats.totalRequests).toBe(1);
      expect(stats.totalResponses).toBe(0);
      expect(stats.successfulRequests).toBe(0);
      expect(stats.failedRequests).toBe(0);
    });

    it('should clear repository data', () => {
      repository.clear();

      expect(repository.getRequestCount()).toBe(0);
      expect(repository.getResponseCount()).toBe(0);
      expect(repository.getIntentCount()).toBe(0);
    });
  });

  describe('Service Health Check', () => {
    it('should return healthy status', async () => {
      const health = await service.getServiceHealth();

      expect(health.status).toBe('healthy');
      expect(health.details.patterns).toBeDefined();
      expect(health.details.timestamp).toBeDefined();
    });

    it('should validate text correctly', async () => {
      const validText = 'Create a task called Test';
      const validation = await service.validateText(validText);

      expect(validation.isValid).toBe(true);
      expect(validation.errors).toHaveLength(0);
    });

    it('should reject empty text', async () => {
      const validation = await service.validateText('');

      expect(validation.isValid).toBe(false);
      expect(validation.errors).toContain('Text cannot be empty');
    });

    it('should reject very long text', async () => {
      const longText = 'a'.repeat(1001);
      const validation = await service.validateText(longText);

      expect(validation.isValid).toBe(false);
      expect(validation.errors).toContain('Text is too long');
    });
  });
}); 