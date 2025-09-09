"""
SmartJARVIS TTS Service (MVP Mock)
Text-to-Speech microservice - simplified for testing
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
    title="SmartJARVIS TTS Service",
    description="Text-to-Speech microservice (Mock for MVP)",
    version="1.0.0-MVP"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class TTSService:
    """Mock TTS service for MVP testing"""
    
    def __init__(self):
        self.voice_profiles = {
            "ruslan": {"speed": 1.0, "pitch": 1.0},
            "anna": {"speed": 0.9, "pitch": 1.1},
            "formal": {"speed": 0.8, "pitch": 0.9}
        }
        logger.info("Mock TTS Service initialized with {} voice profiles", len(self.voice_profiles))
    
    async def synthesize_speech(self, text: str, voice: str = "ruslan", 
                              speed: float = 1.0, session_id: str = "default") -> Dict[str, Any]:
        """Mock speech synthesis"""
        # Simulate processing time based on text length
        processing_time = len(text) * 0.01  # 10ms per character
        await asyncio.sleep(min(processing_time, 2.0))  # Max 2 seconds
        
        # Mock audio generation
        audio_duration = len(text) * 0.1  # 100ms per character
        mock_audio_size = int(audio_duration * 16000 * 2)  # 16kHz, 16-bit
        
        result = {
            "session_id": session_id,
            "text": text,
            "voice": voice,
            "speed": speed,
            "audio_data": f"mock_audio_data_{len(text)}_bytes",  # Mock audio
            "audio_size": mock_audio_size,
            "duration": audio_duration,
            "format": "pcm_16khz",
            "timestamp": int(time.time() * 1000)
        }
        
        logger.info(f"Mock TTS synthesis: session={session_id}, text='{text[:50]}...', duration={audio_duration:.2f}s")
        return result

# Global TTS service instance
tts_service = TTSService()

# Pydantic models
class SynthesisRequest(BaseModel):
    text: str
    voice: str = "ruslan"
    speed: float = 1.0
    session_id: str = "default"

class SynthesisResponse(BaseModel):
    audio_data: str
    audio_size: int
    duration: float
    format: str

# API endpoints
@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {
        "status": "UP",
        "service": "tts-service",
        "version": "1.0.0-MVP",
        "mode": "mock",
        "timestamp": int(time.time() * 1000)
    }

@app.get("/info")
async def service_info():
    """Service information"""
    return {
        "service": "tts-service",
        "description": "Text-to-Speech microservice (Mock for MVP)",
        "version": "1.0.0-MVP",
        "mode": "mock",
        "capabilities": {
            "synthesis": True,
            "voices": list(tts_service.voice_profiles.keys()),
            "formats": ["pcm_16khz"],
            "ssml": False  # Not implemented in mock
        }
    }

@app.post("/synthesize", response_model=SynthesisResponse)
async def synthesize_endpoint(request: SynthesisRequest):
    """Direct synthesis endpoint (for testing)"""
    try:
        if not request.text or request.text.strip() == "":
            raise HTTPException(status_code=400, detail="Text cannot be empty")
        
        result = await tts_service.synthesize_speech(
            request.text,
            request.voice,
            request.speed,
            request.session_id
        )
        
        return SynthesisResponse(
            audio_data=result["audio_data"],
            audio_size=result["audio_size"],
            duration=result["duration"],
            format=result["format"]
        )
        
    except Exception as e:
        logger.error(f"Synthesis failed: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/voices")
async def get_voices():
    """Get available voice profiles"""
    return {
        "voices": tts_service.voice_profiles,
        "default": "ruslan"
    }

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "tts-service",
        "status": "running",
        "version": "1.0.0-MVP"
    }

if __name__ == "__main__":
    logger.info("Starting TTS Service")
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", "8085")),
        log_level="info",
        reload=False
    )
