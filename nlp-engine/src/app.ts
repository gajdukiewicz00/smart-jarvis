import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import morgan from 'morgan';
import dotenv from 'dotenv';

// Domain layer
import { IntentRecognitionService } from './domain/services/IntentRecognitionService';
import { NLPRepository } from './domain/repositories/NLPRepository';

// Application layer
import { ProcessIntentUseCase } from './application/usecases/ProcessIntentUseCase';
import { ExecuteActionUseCase } from './application/usecases/ExecuteActionUseCase';

// Infrastructure layer
import { InMemoryNLPRepository } from './infrastructure/repositories/InMemoryNLPRepository';
import { IntentRecognitionServiceImpl } from './infrastructure/services/IntentRecognitionServiceImpl';

// Presentation layer
import { NLPController } from './presentation/controllers/NLPController';
import { createNLPRoutes } from './presentation/routes/nlpRoutes';

// Load environment variables
dotenv.config();

export class NLPApplication {
  private app: express.Application;
  private port: number;

  constructor() {
    this.port = parseInt(process.env['NLP_ENGINE_PORT'] || '8082', 10);
    this.app = express();
    this.setupMiddleware();
    this.setupDependencies();
    this.setupRoutes();
    this.setupErrorHandling();
  }

  private setupMiddleware(): void {
    // Security middleware
    this.app.use(helmet());
    this.app.use(cors());
    this.app.use(compression());
    
    // Logging middleware
    this.app.use(morgan('combined'));
    
    // Body parsing middleware
    this.app.use(express.json({ limit: '10mb' }));
    this.app.use(express.urlencoded({ extended: true }));
  }

  private setupDependencies(): void {
    // Infrastructure layer - repositories and services
    const nlpRepository: NLPRepository = new InMemoryNLPRepository();
    const intentRecognitionService: IntentRecognitionService = new IntentRecognitionServiceImpl();

    // Application layer - use cases
    const processIntentUseCase = new ProcessIntentUseCase(nlpRepository, intentRecognitionService);
    const executeActionUseCase = new ExecuteActionUseCase(intentRecognitionService, nlpRepository);

    // Presentation layer - controller
    const nlpController = new NLPController(processIntentUseCase, executeActionUseCase);

    // Make dependencies available to routes
    this.app.locals['nlpController'] = nlpController;
  }

  private setupRoutes(): void {
    const nlpController = this.app.locals['nlpController'] as NLPController;

    // API routes
    this.app.use('/api', createNLPRoutes(nlpController));

    // Root endpoint
    this.app.get('/', (req, res) => {
      res.json({
        service: 'NLP Engine',
        version: '1.0.0',
        status: 'running',
        endpoints: {
          process: 'POST /api/process',
          execute: 'POST /api/execute',
          health: 'GET /api/health',
          statistics: 'GET /api/statistics',
          intents: 'GET /api/intents',
          examples: 'GET /api/examples'
        }
      });
    });
  }

  private setupErrorHandling(): void {
    // 404 handler
    this.app.use('*', (req, res) => {
      res.status(404).json({
        error: 'Not found',
        message: `Route ${req.originalUrl} not found`
      });
    });

    // Global error handler
    this.app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
      console.error('Unhandled error:', err);
      res.status(500).json({
        error: 'Internal server error',
        message: process.env['NODE_ENV'] === 'development' ? err.message : 'Something went wrong'
      });
    });
  }

  public start(): void {
    this.app.listen(this.port, () => {
      console.log(`NLP Engine server started on port ${this.port}`);
      console.log(`Environment: ${process.env['NODE_ENV'] || 'development'}`);
      console.log(`Health check available at: http://localhost:${this.port}/api/health`);
      console.log(`API documentation available at: http://localhost:${this.port}/api/examples`);
    });
  }

  public getApp(): express.Application {
    return this.app;
  }
}

// Start the application if this file is run directly
if (require.main === module) {
  const app = new NLPApplication();
  app.start();

  // Graceful shutdown
  process.on('SIGTERM', () => {
    console.log('SIGTERM received, shutting down gracefully');
    process.exit(0);
  });

  process.on('SIGINT', () => {
    console.log('SIGINT received, shutting down gracefully');
    process.exit(0);
  });
} 