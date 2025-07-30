import { Intent } from '../../../domain/entities/Intent';
import { IntentType } from '../../../domain/value-objects/IntentType';

describe('Intent', () => {
  describe('create', () => {
    it('should create an intent with valid parameters', () => {
      const intent = Intent.create(
        IntentType.TASK_CREATE,
        0.9,
        { action: 'create', taskDetails: { title: 'Test task' } },
        'Create a task called Test task',
        { source: 'test' }
      );

      expect(intent.id).toBeDefined();
      expect(intent.type.getValue()).toBe(IntentType.TASK_CREATE);
      expect(intent.confidence.getValue()).toBe(0.9);
      expect(intent.text).toBe('Create a task called Test task');
      expect(intent.context).toEqual({ source: 'test' });
    });

    it('should create an unknown intent for invalid text', () => {
      const intent = Intent.createUnknown('Invalid command', { source: 'test' });

      expect(intent.id).toBeDefined();
      expect(intent.type.getValue()).toBe(IntentType.UNKNOWN);
      expect(intent.confidence.getValue()).toBe(0.0);
      expect(intent.text).toBe('Invalid command');
      expect(intent.context).toEqual({ source: 'test' });
    });
  });

  describe('business logic methods', () => {
    let taskIntent: Intent;
    let unknownIntent: Intent;

    beforeEach(() => {
      taskIntent = Intent.create(
        IntentType.TASK_CREATE,
        0.9,
        { action: 'create', taskDetails: { title: 'Test task' } },
        'Create a task called Test task'
      );

      unknownIntent = Intent.createUnknown('Invalid command');
    });

    it('should identify task intents correctly', () => {
      expect(taskIntent.isTaskIntent()).toBe(true);
      expect(unknownIntent.isTaskIntent()).toBe(false);
    });

    it('should identify unknown intents correctly', () => {
      expect(taskIntent.isUnknown()).toBe(false);
      expect(unknownIntent.isUnknown()).toBe(true);
    });

    it('should check reliability correctly', () => {
      expect(taskIntent.isReliable()).toBe(true);
      expect(unknownIntent.isReliable()).toBe(false);
    });

    it('should check for entities correctly', () => {
      expect(taskIntent.hasEntities()).toBe(true);
      expect(unknownIntent.hasEntities()).toBe(false);
    });

    it('should get entity keys correctly', () => {
      const keys = taskIntent.getEntityKeys();
      expect(keys).toContain('action');
      expect(keys).toContain('taskDetails');
    });

    it('should get task details correctly', () => {
      const taskDetails = taskIntent.getTaskDetails();
      expect(taskDetails).toEqual({ title: 'Test task' });
    });

    it('should return null for task details on non-task intent', () => {
      const taskDetails = unknownIntent.getTaskDetails();
      expect(taskDetails).toBeNull();
    });
  });

  describe('entity operations', () => {
    let intent: Intent;

    beforeEach(() => {
      intent = Intent.create(
        IntentType.TASK_CREATE,
        0.9,
        { action: 'create', taskDetails: { title: 'Test task' } },
        'Create a task called Test task'
      );
    });

    it('should get entity by key', () => {
      const action = intent.getEntity('action');
      expect(action).toBe('create');
    });

    it('should check if entity exists', () => {
      expect(intent.hasEntity('action')).toBe(true);
      expect(intent.hasEntity('nonexistent')).toBe(false);
    });

    it('should return undefined for non-existent entity', () => {
      const entity = intent.getEntity('nonexistent');
      expect(entity).toBeUndefined();
    });
  });

  describe('serialization', () => {
    it('should serialize to JSON correctly', () => {
      const intent = Intent.create(
        IntentType.TASK_CREATE,
        0.9,
        { action: 'create' },
        'Create a task',
        { source: 'test' }
      );

      const json = intent.toJSON();
      expect(json.id).toBe(intent.id);
      expect(json.type.getValue()).toBe(IntentType.TASK_CREATE);
      expect(json.confidence.getValue()).toBe(0.9);
      expect(json.text).toBe('Create a task');
      expect(json.context).toEqual({ source: 'test' });
    });

    it('should deserialize from JSON correctly', () => {
      const originalIntent = Intent.create(
        IntentType.TASK_CREATE,
        0.9,
        { action: 'create' },
        'Create a task',
        { source: 'test' }
      );

      const json = originalIntent.toJSON();
      const deserializedIntent = Intent.fromJSON(json);

      expect(deserializedIntent.id).toBe(originalIntent.id);
      expect(deserializedIntent.type.getValue()).toBe(originalIntent.type.getValue());
      expect(deserializedIntent.confidence.getValue()).toBe(originalIntent.confidence.getValue());
      expect(deserializedIntent.text).toBe(originalIntent.text);
      expect(deserializedIntent.context).toEqual(originalIntent.context);
    });
  });
}); 