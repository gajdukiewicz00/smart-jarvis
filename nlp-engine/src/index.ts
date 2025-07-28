import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import morgan from 'morgan';
import dotenv from 'dotenv';
import { createLogger, format, transports } from 'winston';

import { IntentProcessor } from './services/IntentProcessor';
import { NLPService } from './services/NLPService';
import { TaskIntentHandler } from './intents/TaskIntentHandler';
import { ReminderIntentHandler } from './intents/ReminderIntentHandler';
import { SystemIntentHandler } from './intents/SystemIntentHandler';

// Load environment variables
dotenv.config();

// Configure logger
const logger = createLogger({
    level: process.env['LOG_LEVEL'] || 'info',
    format: format.combine(
        format.timestamp(),
        format.errors({ stack: true }),
        format.json()
    ),
    transports: [
        new transports.Console({
            format: format.combine(
                format.colorize(),
                format.simple()
            )
        }),
        new transports.File({ filename: 'logs/error.log', level: 'error' }),
        new transports.File({ filename: 'logs/combined.log' })
    ]
});

// Create Express app
const app = express();
const PORT = process.env['NLP_ENGINE_PORT'] || 8082;
const TASK_SERVICE_URL = process.env['TASK_SERVICE_URL'] || 'http://localhost:8081';

// Middleware
app.use(helmet());
app.use(cors());
app.use(compression());
app.use(morgan('combined'));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

// Initialize services
const intentProcessor = new IntentProcessor();
const nlpService = new NLPService(intentProcessor, TASK_SERVICE_URL);

// Register intent handlers
const taskHandler = new TaskIntentHandler();
const reminderHandler = new ReminderIntentHandler();
const systemHandler = new SystemIntentHandler();

intentProcessor.registerHandler('task', taskHandler);
intentProcessor.registerHandler('reminder', reminderHandler);
intentProcessor.registerHandler('system', systemHandler);

// Health check endpoint
app.get('/health', (req, res) => {
    res.status(200).json({
        status: 'healthy',
        service: 'nlp-engine',
        timestamp: new Date().toISOString(),
        uptime: process.uptime(),
        taskServiceUrl: TASK_SERVICE_URL
    });
});

// Process intent endpoint
app.post('/api/process', async (req, res) => {
    try {
        const { text, context, execute = false } = req.body;
        
        if (!text) {
            return res.status(400).json({
                error: 'Text input is required'
            });
        }
        
        logger.info(`Processing intent for text: ${text}`);
        
        const result = await nlpService.processIntent(text, context);
        
        // Execute action if requested
        if (execute && result.intent.startsWith('task_')) {
            const actionResult = await nlpService.executeTaskAction(result.intent, result.entities);
            return res.json({
                success: true,
                data: actionResult,
                original: result
            });
        }
        
        logger.info(`Intent processed successfully: ${result.intent}`);
        
        return res.json({
            success: true,
            data: result
        });
        
    } catch (error) {
        logger.error('Error processing intent:', error);
        return res.status(500).json({
            error: 'Internal server error',
            message: error instanceof Error ? error.message : 'Unknown error'
        });
    }
});

// Execute action endpoint
app.post('/api/execute', async (req, res) => {
    try {
        const { intent, entities } = req.body;
        
        if (!intent) {
            return res.status(400).json({
                error: 'Intent is required'
            });
        }
        
        logger.info(`Executing action for intent: ${intent}`);
        
        const result = await nlpService.executeTaskAction(intent, entities);
        
        logger.info(`Action executed successfully: ${result.intent}`);
        
        return res.json({
            success: true,
            data: result
        });
        
    } catch (error) {
        logger.error('Error executing action:', error);
        return res.status(500).json({
            error: 'Internal server error',
            message: error instanceof Error ? error.message : 'Unknown error'
        });
    }
});

// Get supported intents endpoint
app.get('/api/intents', (req, res) => {
    try {
        const intents = intentProcessor.getSupportedIntents();
        res.json({
            success: true,
            data: intents
        });
    } catch (error) {
        logger.error('Error getting intents:', error);
        res.status(500).json({
            error: 'Internal server error'
        });
    }
});

// Task service status endpoint
app.get('/api/task-service/status', async (req, res) => {
    try {
        const isAvailable = await nlpService['taskServiceClient'].ping();
        res.json({
            success: true,
            data: {
                available: isAvailable,
                url: TASK_SERVICE_URL
            }
        });
    } catch (error) {
        logger.error('Error checking task service status:', error);
        res.status(500).json({
            error: 'Internal server error'
        });
    }
});

// Voice command examples endpoint
app.get('/api/examples', (req, res) => {
    const examples = {
        task_management: [
            "Create a task called 'Buy groceries'",
            "Add a high priority task 'Finish project report'",
            "Show my tasks",
            "List pending tasks",
            "Complete task 'Buy groceries'",
            "Update task priority to urgent",
            "Delete task 'Old task'",
            "What are my task statistics?",
            "How many tasks do I have?",
            "Show completed tasks"
        ],
        natural_language: [
            "I need to buy groceries",
            "Remind me to call the doctor",
            "What tasks are due today?",
            "Mark the project task as done",
            "Set priority to high for the meeting task",
            "Show me all my pending work"
        ]
    };
    
    res.json({
        success: true,
        data: examples
    });
});

// Error handling middleware
app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
    logger.error('Unhandled error:', err);
    res.status(500).json({
        error: 'Internal server error',
        message: process.env['NODE_ENV'] === 'development' ? err.message : 'Something went wrong'
    });
});

// 404 handler
app.use('*', (req, res) => {
    res.status(404).json({
        error: 'Not found',
        message: `Route ${req.originalUrl} not found`
    });
});

// Start server
app.listen(PORT, () => {
    logger.info(`NLP Engine server started on port ${PORT}`);
    logger.info(`Environment: ${process.env['NODE_ENV'] || 'development'}`);
    logger.info(`Task Service URL: ${TASK_SERVICE_URL}`);
    logger.info(`Health check available at: http://localhost:${PORT}/health`);
    logger.info(`API documentation available at: http://localhost:${PORT}/api/examples`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    logger.info('SIGTERM received, shutting down gracefully');
    process.exit(0);
});

process.on('SIGINT', () => {
    logger.info('SIGINT received, shutting down gracefully');
    process.exit(0);
});

export default app; 