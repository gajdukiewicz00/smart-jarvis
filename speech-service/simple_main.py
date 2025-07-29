#!/usr/bin/env python3
"""
SmartJARVIS Speech Service - Simplified Version
Basic HTTP endpoints for testing
"""

import asyncio
import logging
import os
from typing import Optional
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
from loguru import logger
import httpx

# Configure logging
logging.basicConfig(level=logging.INFO)
logger.add("logs/speech_service.log", rotation="1 day", retention="7 days")

# Initialize FastAPI app
app = FastAPI(
    title="SmartJARVIS Speech Service",
    description="Speech-to-text and text-to-speech service for JARVIS",
    version="1.0.0"
)

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configuration
NLP_ENGINE_URL = os.getenv("NLP_ENGINE_URL", "http://localhost:8082")
SPEECH_SERVICE_PORT = int(os.getenv("SPEECH_SERVICE_PORT", "8083"))

# Pydantic models
class TextToSpeechRequest(BaseModel):
    text: str
    voice: Optional[str] = "en"
    rate: Optional[int] = 150

class SpeechToTextResponse(BaseModel):
    text: str
    confidence: float
    language: str

class HealthResponse(BaseModel):
    status: str
    service: str
    timestamp: str
    nlp_engine_url: str

@app.on_event("startup")
async def startup_event():
    """Initialize components on startup"""
    logger.info("Starting SmartJARVIS Speech Service (Simplified)...")
    logger.info("Speech Service started successfully")

@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup on shutdown"""
    logger.info("Shutting down Speech Service...")

@app.get("/health", response_model=HealthResponse)
async def health_check():
    """Health check endpoint"""
    return HealthResponse(
        status="healthy",
        service="speech-service",
        timestamp=asyncio.get_event_loop().time().__str__(),
        nlp_engine_url=NLP_ENGINE_URL
    )

@app.post("/api/speech-to-text", response_model=SpeechToTextResponse)
async def speech_to_text():
    """
    Mock speech-to-text endpoint
    """
    try:
        logger.info("Mock speech-to-text called")
        
        # Return mock response
        response = SpeechToTextResponse(
            text="Hello, this is a test message",
            confidence=0.95,
            language="en"
        )
        
        logger.info(f"Mock speech-to-text completed: {response.text}")
        return response
        
    except Exception as e:
        logger.error(f"Error in speech-to-text: {e}")
        raise HTTPException(status_code=500, detail=f"Speech-to-text error: {str(e)}")

@app.post("/api/text-to-speech")
async def text_to_speech(request: TextToSpeechRequest):
    """
    Mock text-to-speech endpoint
    """
    try:
        logger.info(f"Mock text-to-speech: {request.text[:50]}...")
        
        # Return mock response
        return {
            "success": True,
            "message": f"Text '{request.text}' would be converted to speech",
            "voice": request.voice,
            "rate": request.rate
        }
        
    except Exception as e:
        logger.error(f"Error in text-to-speech: {e}")
        raise HTTPException(status_code=500, detail=f"Text-to-speech error: {str(e)}")

@app.get("/api/voices")
async def get_available_voices():
    """
    Get available TTS voices/languages
    """
    try:
        # Return supported languages
        supported_languages = {
            "en": "English",
            "ru": "Russian", 
            "es": "Spanish",
            "fr": "French",
            "de": "German"
        }
        
        return {
            "voices": supported_languages,
            "default": "en",
            "message": "Mock TTS service"
        }
        
    except Exception as e:
        logger.error(f"Error getting voices: {e}")
        raise HTTPException(status_code=500, detail=f"Error getting voices: {str(e)}")

@app.get("/api/nlp-status")
async def get_nlp_status():
    """
    Check NLP Engine status
    """
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(f"{NLP_ENGINE_URL}/health", timeout=5.0)
            
            return {
                "success": response.status_code == 200,
                "nlp_engine_status": "healthy" if response.status_code == 200 else "unhealthy",
                "nlp_engine_url": NLP_ENGINE_URL
            }
            
    except Exception as e:
        logger.error(f"Error checking NLP Engine status: {e}")
        return {
            "success": False,
            "nlp_engine_status": "unreachable",
            "nlp_engine_url": NLP_ENGINE_URL,
            "error": str(e)
        }

if __name__ == "__main__":
    uvicorn.run(
        "simple_main:app",
        host="0.0.0.0",
        port=SPEECH_SERVICE_PORT,
        reload=True
    )