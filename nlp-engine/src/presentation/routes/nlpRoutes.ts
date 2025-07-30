import { Router } from 'express';
import { NLPController } from '../controllers/NLPController';

export function createNLPRoutes(controller: NLPController): Router {
  const router = Router();

  // Process intent endpoint
  router.post('/process', async (req, res) => {
    await controller.processIntent(req, res);
  });

  // Execute action endpoint
  router.post('/execute', async (req, res) => {
    await controller.executeAction(req, res);
  });

  // Health check endpoint
  router.get('/health', async (req, res) => {
    await controller.getHealth(req, res);
  });

  // Statistics endpoint
  router.get('/statistics', async (req, res) => {
    await controller.getStatistics(req, res);
  });

  // Get supported intents endpoint
  router.get('/intents', async (req, res) => {
    await controller.getIntents(req, res);
  });

  // Get examples endpoint
  router.get('/examples', async (req, res) => {
    await controller.getExamples(req, res);
  });

  return router;
}