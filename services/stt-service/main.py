"""
SmartJARVIS STT Service (MVP Mock)
Speech-to-Text microservice - simplified for testing
"""

import asyncio
import logging
import os
import time
from typing import Dict, Any, Optional

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# FastAPI app
app = FastAPI(
    title="SmartJARVIS STT Service",
    description="Speech-to-Text microservice (Mock for MVP)",
    version="1.0.0-MVP"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class STTService:
    """Mock STT service for MVP testing"""
    
    def __init__(self):
        self.mock_responses = {
            "small": "Привет, Джарвис",
            "medium": "Помощь",
            "large": "Добавь задачу позвонить маме",
            "xlarge": "Включи свет в гостиной",
            "default": "К вашим услугам"
        }
        logger.info("Mock STT Service initialized")
    
    async def transcribe_audio(self, audio_data: bytes, session_id: str, user_id: str) -> Dict[str, Any]:
        """Mock transcription - returns predefined responses"""
        # Simulate processing time
        await asyncio.sleep(0.1)
        
        # Mock transcription based on audio size
        audio_size = len(audio_data)
        
        if audio_size < 1000:
            transcript = self.mock_responses["small"]
        elif audio_size < 5000:
            transcript = self.mock_responses["medium"]
        elif audio_size < 10000:
            transcript = self.mock_responses["large"]
        elif audio_size < 20000:
            transcript = self.mock_responses["xlarge"]
        else:
            transcript = self.mock_responses["default"]
        
        result = {
            "session_id": session_id,
            "user_id": user_id,
            "transcript": transcript,
            "confidence": 0.95,
            "language": "ru",
            "duration": audio_size / 16000.0,  # Mock duration
            "timestamp": int(time.time() * 1000)
        }
        
        logger.info(f"Mock transcription: session={session_id}, text='{transcript}'")
        return result

# Global STT service instance
stt_service = STTService()

# Pydantic models
class TranscriptionRequest(BaseModel):
    audio_data: str  # Base64 encoded for simplicity
    session_id: str
    user_id: str = "anonymous"

class TranscriptionResponse(BaseModel):
    transcript: str
    confidence: float
    language: str
    duration: float

# API endpoints
@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "UP",
        "service": "stt-service",
        "version": "1.0.0-MVP",
        "mode": "mock",
        "timestamp": int(time.time() * 1000)
    }

@app.get("/info")
async def service_info():
    """Service information"""
    return {
        "service": "stt-service",
        "description": "Speech-to-Text microservice (Mock for MVP)",
        "version": "1.0.0-MVP",
        "mode": "mock",
        "capabilities": {
            "transcription": True,
            "languages": ["ru"],
            "mock_responses": list(stt_service.mock_responses.keys())
        }
    }

@app.post("/transcribe", response_model=TranscriptionResponse)
async def transcribe_endpoint(request: TranscriptionRequest):
    """Direct transcription endpoint (for testing)"""
    try:
        # Convert base64 to bytes (simplified)
        audio_bytes = request.audio_data.encode('utf-8')
        
        result = await stt_service.transcribe_audio(
            audio_bytes,
            request.session_id,
            request.user_id
        )
        
        return TranscriptionResponse(
            transcript=result["transcript"],
            confidence=result["confidence"],
            language=result["language"],
            duration=result["duration"]
        )
        
    except Exception as e:
        logger.error(f"Transcription failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "stt-service",
        "status": "running",
        "version": "1.0.0-MVP"
    }

if __name__ == "__main__":
    logger.info("Starting STT Service")
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", "8082")),
        log_level="info",
        reload=False
    )