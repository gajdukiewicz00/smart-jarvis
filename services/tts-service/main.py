"""
SmartJARVIS TTS Service (MVP Mock)
Text-to-Speech microservice - simplified for testing
"""

import asyncio
import logging
import os
import time
from typing import Dict, Any, Optional
import json

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
    """Mock TTS service for MVP testing with barge-in support"""
    
    def __init__(self):
        self.voice_profiles = {
            "ruslan": {"speed": 1.0, "pitch": 1.0},
            "anna": {"speed": 0.9, "pitch": 1.1},
            "formal": {"speed": 0.8, "pitch": 0.9}
        }
        self.active_synthesis = {}  # sessionId -> asyncio.Task
        logger.info("Mock TTS Service initialized with {} voice profiles", len(self.voice_profiles))
    
    async def synthesize_speech_with_bargein(self, text: str, voice: str = "ruslan", 
                                           speed: float = 1.0, session_id: str = "default") -> Dict[str, Any]:
        """Mock speech synthesis with barge-in support"""
        logger.info(f"Starting TTS synthesis: session={session_id}, text='{text[:50]}...'")
        
        try:
            # Create cancellable task
            synthesis_task = asyncio.create_task(self._synthesize_internal(text, voice, speed, session_id))
            self.active_synthesis[session_id] = synthesis_task
            
            # Wait for completion or cancellation
            result = await synthesis_task
            
            logger.info(f"TTS synthesis completed: session={session_id}")
            return result
            
        except asyncio.CancelledError:
            logger.info(f"TTS synthesis cancelled by barge-in: session={session_id}")
            return {
                "session_id": session_id,
                "cancelled": True,
                "reason": "barge_in",
                "timestamp": int(time.time() * 1000)
            }
        finally:
            # Clean up
            self.active_synthesis.pop(session_id, None)
    
    async def _synthesize_internal(self, text: str, voice: str, speed: float, session_id: str) -> Dict[str, Any]:
        """Internal synthesis method"""
        # Simulate processing time based on text length
        processing_time = len(text) * 0.01  # 10ms per character
        audio_duration = len(text) * 0.1    # 100ms per character
        
        # Simulate streaming synthesis (can be interrupted)
        chunks = max(1, int(audio_duration * 10))  # 100ms chunks
        chunk_duration = processing_time / chunks
        
        for i in range(chunks):
            await asyncio.sleep(chunk_duration)
            # Check if still active (not cancelled)
            if session_id not in self.active_synthesis:
                raise asyncio.CancelledError()
        
        # Mock audio generation
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
        
        return result
    
    def cancel_synthesis(self, session_id: str) -> bool:
        """Cancel active synthesis for session"""
        if session_id in self.active_synthesis:
            task = self.active_synthesis[session_id]
            if not task.done():
                task.cancel()
                logger.info(f"TTS synthesis cancelled: session={session_id}")
                return True
        return False
    
    def is_synthesis_active(self, session_id: str) -> bool:
        """Check if synthesis is active for session"""
        return session_id in self.active_synthesis and not self.active_synthesis[session_id].done()

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

@app.post("/cancel/{session_id}")
async def cancel_synthesis(session_id: str):
    """Cancel active synthesis for session"""
    try:
        cancelled = tts_service.cancel_synthesis(session_id)
        return {
            "session_id": session_id,
            "cancelled": cancelled,
            "timestamp": int(time.time() * 1000)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/status/{session_id}")
async def get_synthesis_status(session_id: str):
    """Get synthesis status for session"""
    try:
        is_active = tts_service.is_synthesis_active(session_id)
        return {
            "session_id": session_id,
            "is_active": is_active,
            "timestamp": int(time.time() * 1000)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "tts-service",
        "status": "running",
        "version": "1.0.0-MVP",
        "features": {
            "barge_in": True,
            "cancellation": True,
            "streaming": True
        }
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
