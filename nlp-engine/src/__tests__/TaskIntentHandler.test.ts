import { TaskIntentHandler } from '../intents/TaskIntentHandler';

describe('TaskIntentHandler', () => {
    let handler: TaskIntentHandler;

    beforeEach(() => {
        handler = new TaskIntentHandler();
    });

    describe('Create task patterns', () => {
        const createPatterns = [
            'Create a task called "Buy groceries"',
            'Add a task "Finish project"',
            'New task "Call doctor"',
            'Make a task "Prepare presentation"',
            'Set task "Review code"',
            'Create todo "Buy milk"',
            'Add todo "Send email"'
        ];

        test.each(createPatterns)('should recognize create pattern: %s', async (text) => {
            const result = await handler.handle(text);
            
            expect(result.intent).toBe('task_create');
            expect(result.confidence).toBeGreaterThan(0.9);
            expect(result.entities.action).toBe('create');
            expect(result.entities.type).toBe('task');
            expect(result.entities.extractedDetails).toBeDefined();
        });
    });

    describe('List task patterns', () => {
        const listPatterns = [
            'List my tasks',
            'Show all tasks',
            'Get tasks',
            'Display tasks',
            'What tasks do I have?',
            'My tasks',
            'All tasks',
            'Pending tasks',
            'Completed tasks'
        ];

        test.each(listPatterns)('should recognize list pattern: %s', async (text) => {
            const result = await handler.handle(text);
            
            expect(result.intent).toBe('task_list');
            expect(result.confidence).toBeGreaterThan(0.8);
            expect(result.entities.action).toBe('list');
            expect(result.entities.type).toBe('task');
        });
    });

    describe('Statistics patterns', () => {
        const statsPatterns = [
            'Show task statistics',
            'Task stats',
            'Task summary',
            'Task overview',
            'How many tasks do I have?',
            'Task count',
            'Task summary'
        ];

        test.each(statsPatterns)('should recognize statistics pattern: %s', async (text) => {
            const result = await handler.handle(text);
            
            expect(result.intent).toBe('task_statistics');
            expect(result.confidence).toBeGreaterThan(0.8);
            expect(result.entities.action).toBe('statistics');
            expect(result.entities.type).toBe('task');
        });
    });

    describe('Complete task patterns', () => {
        const completePatterns = [
            'Complete task "Buy groceries"',
            'Finish task "Project report"',
            'Done task "Call doctor"',
            'Mark task as complete',
            'Task "Review code" done',
            'Task "Send email" finished'
        ];

        test.each(completePatterns)('should recognize complete pattern: %s', async (text) => {
            const result = await handler.handle(text);
            
            expect(result.intent).toBe('task_complete');
            expect(result.confidence).toBeGreaterThan(0.8);
            expect(result.entities.action).toBe('complete');
            expect(result.entities.type).toBe('task');
        });
    });

    describe('Update task patterns', () => {
        const updatePatterns = [
            'Update task "Buy groceries"',
            'Modify task "Project report"',
            'Change task "Call doctor"',
            'Edit task "Review code"'
        ];

        test.each(updatePatterns)('should recognize update pattern: %s', async (text) => {
            const result = await handler.handle(text);
            
            expect(result.intent).toBe('task_update');
            expect(result.confidence).toBeGreaterThan(0.7);
            expect(result.entities.action).toBe('update');
            expect(result.entities.type).toBe('task');
        });
    });

    describe('Delete task patterns', () => {
        const deletePatterns = [
            'Delete task "Old task"',
            'Remove task "Completed task"',
            'Cancel task "Unnecessary task"',
            'Drop task "Test task"'
        ];

        test.each(deletePatterns)('should recognize delete pattern: %s', async (text) => {
            const result = await handler.handle(text);
            
            expect(result.intent).toBe('task_delete');
            expect(result.confidence).toBeGreaterThan(0.7);
            expect(result.entities.action).toBe('delete');
            expect(result.entities.type).toBe('task');
        });
    });

    describe('Priority patterns', () => {
        const priorityPatterns = [
            'Set priority to high for task "Important task"',
            'Task "Urgent task" priority urgent',
            'Low priority for task "Minor task"',
            'Medium priority task "Normal task"',
            'High priority task "Critical task"',
            'Urgent priority task "Emergency task"'
        ];

        test.each(priorityPatterns)('should recognize priority pattern: %s', async (text) => {
            const result = await handler.handle(text);
            
            expect(result.intent).toBe('task_priority');
            expect(result.confidence).toBeGreaterThan(0.8);
            expect(result.entities.action).toBe('priority');
            expect(result.entities.type).toBe('task');
            expect(result.entities.priority).toBeDefined();
        });
    });

    describe('Status patterns', () => {
        const statusPatterns = [
            'Set task "Task 1" status to pending',
            'Task "Task 2" status in progress',
            'Mark task "Task 3" as completed',
            'Task "Task 4" status cancelled',
            'Pending status for task "Task 5"',
            'In progress task "Task 6"',
            'Completed status task "Task 7"'
        ];

        test.each(statusPatterns)('should recognize status pattern: %s', async (text) => {
            const result = await handler.handle(text);
            
            expect(result.intent).toBe('task_status');
            expect(result.confidence).toBeGreaterThan(0.8);
            expect(result.entities.action).toBe('status');
            expect(result.entities.type).toBe('task');
            expect(result.entities.status).toBeDefined();
        });
    });

    describe('Unknown patterns', () => {
        const unknownPatterns = [
            'Hello world',
            'What is the weather?',
            'Play music',
            'Open browser',
            'Random text'
        ];

        test.each(unknownPatterns)('should return unknown intent for: %s', async (text) => {
            const result = await handler.handle(text);
            
            expect(result.intent).toBe('task_unknown');
            expect(result.confidence).toBeLessThan(0.6);
            expect(result.entities.type).toBe('task');
        });
    });

    describe('Task details extraction', () => {
        test('should extract task details with priority', async () => {
            const result = await handler.handle('Create a high priority task "Important meeting"');
            
            expect(result.intent).toBe('task_create');
            expect(result.entities.extractedDetails.title).toBe('Important meeting');
            expect(result.entities.extractedDetails.priority).toBe('HIGH');
        });

        test('should extract task details with urgent priority', async () => {
            const result = await handler.handle('Add urgent task "Emergency call"');
            
            expect(result.intent).toBe('task_create');
            expect(result.entities.extractedDetails.title).toBe('Emergency call');
            expect(result.entities.extractedDetails.priority).toBe('URGENT');
        });

        test('should extract task details with low priority', async () => {
            const result = await handler.handle('New low priority task "Minor task"');
            
            expect(result.intent).toBe('task_create');
            expect(result.entities.extractedDetails.title).toBe('Minor task');
            expect(result.entities.extractedDetails.priority).toBe('LOW');
        });
    });

    describe('List filter extraction', () => {
        test('should extract pending filter', async () => {
            const result = await handler.handle('Show pending tasks');
            
            expect(result.intent).toBe('task_list');
            expect(result.entities.filter).toBe('PENDING');
        });

        test('should extract completed filter', async () => {
            const result = await handler.handle('List completed tasks');
            
            expect(result.intent).toBe('task_list');
            expect(result.entities.filter).toBe('COMPLETED');
        });

        test('should extract in progress filter', async () => {
            const result = await handler.handle('Show tasks in progress');
            
            expect(result.intent).toBe('task_list');
            expect(result.entities.filter).toBe('IN_PROGRESS');
        });
    });

    describe('Task identifier extraction', () => {
        test('should extract task ID', async () => {
            const result = await handler.handle('Complete task 123e4567-e89b-12d3-a456-426614174000');
            
            expect(result.intent).toBe('task_complete');
            expect(result.entities.taskIdentifier).toBe('123e4567-e89b-12d3-a456-426614174000');
        });

        test('should extract task title', async () => {
            const result = await handler.handle('Complete task "Buy groceries"');
            
            expect(result.intent).toBe('task_complete');
            expect(result.entities.taskIdentifier).toBe('Buy groceries');
        });

        test('should extract task title without quotes', async () => {
            const result = await handler.handle('Finish task Buy groceries');
            
            expect(result.intent).toBe('task_complete');
            expect(result.entities.taskIdentifier).toBe('Buy groceries');
        });
    });
}); 