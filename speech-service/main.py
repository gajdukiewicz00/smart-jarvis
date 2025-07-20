#!/usr/bin/env python3
"""
SmartJARVIS Speech Service
Handles speech-to-text and text-to-speech functionality
"""

import asyncio
import logging
from typing import Optional
from fastapi import FastAPI, HTTPException, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
from loguru import logger
import httpx
import os

from speech_recognition import Recognizer, Microphone
import pyttsx3
import whisper
import tempfile

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
    voice: Optional[str] = "default"
    rate: Optional[int] = 150
    volume: Optional[float] = 1.0

class SpeechToTextResponse(BaseModel):
    text: str
    confidence: float
    language: str

class HealthResponse(BaseModel):
    status: str
    service: str
    timestamp: str
    nlp_engine_url: str

class VoiceCommandResponse(BaseModel):
    original_text: str
    nlp_result: dict
    tts_response: str

# Initialize speech components
recognizer = Recognizer()
microphone = None  # Initialize lazily
whisper_model = None
tts_engine = None

def get_microphone():
    """Get microphone instance (lazy initialization)"""
    global microphone
    if microphone is None:
        try:
            microphone = Microphone()
        except Exception as e:
            logger.warning(f"Could not initialize microphone: {e}")
            # Return a dummy microphone for testing
            microphone = None
    return microphone

def initialize_speech_components():
    """Initialize speech recognition and TTS components"""
    global whisper_model, tts_engine
    
    try:
        # Initialize Whisper model
        logger.info("Loading Whisper model...")
        whisper_model = whisper.load_model("base")
        logger.info("Whisper model loaded successfully")
        
        # TTS engine will be initialized lazily when needed
        logger.info("TTS engine will be initialized on demand")
        
    except Exception as e:
        logger.error(f"Error initializing speech components: {e}")
        raise

def get_tts_engine():
    """Get TTS engine instance (lazy initialization)"""
    global tts_engine
    if tts_engine is None:
        try:
            logger.info("Initializing TTS engine...")
            # Temporarily disable TTS to avoid espeak issues
            logger.warning("TTS engine disabled due to espeak issues")
            tts_engine = None
        except Exception as e:
            logger.error(f"Error initializing TTS engine: {e}")
            tts_engine = None
    return tts_engine

async def process_with_nlp(text: str) -> dict:
    """Process text with NLP Engine"""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{NLP_ENGINE_URL}/api/process",
                json={
                    "text": text,
                    "context": {},
                    "execute": False
                },
                timeout=10.0
            )
            
            if response.status_code == 200:
                return response.json()
            else:
                logger.error(f"NLP Engine error: {response.status_code} - {response.text}")
                return {
                    "success": False,
                    "error": f"NLP Engine returned {response.status_code}"
                }
                
    except Exception as e:
        logger.error(f"Error communicating with NLP Engine: {e}")
        return {
            "success": False,
            "error": f"Communication error: {str(e)}"
        }

async def execute_nlp_action(intent: str, entities: dict) -> dict:
    """Execute action with NLP Engine"""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(
                f"{NLP_ENGINE_URL}/api/execute",
                json={
                    "intent": intent,
                    "entities": entities
                },
                timeout=10.0
            )
            
            if response.status_code == 200:
                return response.json()
            else:
                logger.error(f"NLP Engine execute error: {response.status_code} - {response.text}")
                return {
                    "success": False,
                    "error": f"NLP Engine execute returned {response.status_code}"
                }
                
    except Exception as e:
        logger.error(f"Error executing action with NLP Engine: {e}")
        return {
            "success": False,
            "error": f"Execute error: {str(e)}"
        }

@app.on_event("startup")
async def startup_event():
    """Initialize components on startup"""
    logger.info("Starting SmartJARVIS Speech Service...")
    initialize_speech_components()
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
async def speech_to_text(audio_file: UploadFile = File(...)):
    """
    Convert speech to text using Whisper
    """
    try:
        logger.info(f"Processing speech-to-text for file: {audio_file.filename}")
        
        # Save uploaded file temporarily
        with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_file:
            content = await audio_file.read()
            temp_file.write(content)
            temp_file_path = temp_file.name
        
        try:
            # Transcribe using Whisper
            result = whisper_model.transcribe(temp_file_path)
            
            # Clean up temporary file
            os.unlink(temp_file_path)
            
            response = SpeechToTextResponse(
                text=result["text"].strip(),
                confidence=result.get("confidence", 0.0),
                language=result.get("language", "en")
            )
            
            logger.info(f"Speech-to-text completed: {response.text[:50]}...")
            return response
            
        except Exception as e:
            # Clean up temporary file on error
            if os.path.exists(temp_file_path):
                os.unlink(temp_file_path)
            raise e
            
    except Exception as e:
        logger.error(f"Error in speech-to-text: {e}")
        raise HTTPException(status_code=500, detail=f"Speech-to-text error: {str(e)}")

@app.post("/api/text-to-speech")
async def text_to_speech(request: TextToSpeechRequest):
    """
    Convert text to speech
    """
    try:
        logger.info(f"Processing text-to-speech: {request.text[:50]}...")
        
        # TTS is temporarily disabled
        logger.warning("TTS functionality is temporarily disabled")
        return {"success": False, "message": "TTS functionality is temporarily disabled"}
        
    except Exception as e:
        logger.error(f"Error in text-to-speech: {e}")
        raise HTTPException(status_code=500, detail=f"Text-to-speech error: {str(e)}")

@app.post("/api/voice-command", response_model=VoiceCommandResponse)
async def process_voice_command(audio_file: UploadFile = File(...)):
    """
    Process voice command: speech-to-text -> NLP processing -> text-to-speech response
    """
    try:
        logger.info(f"Processing voice command from file: {audio_file.filename}")
        
        # Step 1: Speech to text
        stt_response = await speech_to_text(audio_file)
        original_text = stt_response.text
        
        if not original_text.strip():
            return VoiceCommandResponse(
                original_text="",
                nlp_result={"success": False, "error": "No speech detected"},
                tts_response="I didn't hear anything. Please try again."
            )
        
        logger.info(f"Recognized speech: {original_text}")
        
        # Step 2: Process with NLP Engine
        nlp_result = await process_with_nlp(original_text)
        
        if not nlp_result.get("success", False):
            tts_response = "I'm sorry, I couldn't process your request. Please try again."
        else:
            nlp_data = nlp_result.get("data", {})
            tts_response = nlp_data.get("response", "I understood your request.")
            
            # Step 3: Execute action if it's a task-related command
            if nlp_data.get("intent", "").startswith("task_"):
                execute_result = await execute_nlp_action(
                    nlp_data["intent"], 
                    nlp_data.get("entities", {})
                )
                
                if execute_result.get("success", False):
                    execute_data = execute_result.get("data", {})
                    tts_response = execute_data.get("response", tts_response)
                else:
                    tts_response = "I encountered an error while processing your request."
        
        # Step 4: Text to speech response
        await text_to_speech(TextToSpeechRequest(text=tts_response))
        
        return VoiceCommandResponse(
            original_text=original_text,
            nlp_result=nlp_result,
            tts_response=tts_response
        )
        
    except Exception as e:
        logger.error(f"Error in voice command processing: {e}")
        raise HTTPException(status_code=500, detail=f"Voice command error: {str(e)}")

@app.post("/api/listen")
async def listen_for_speech():
    """
    Listen for speech input using microphone
    """
    try:
        logger.info("Starting speech recognition...")
        
        microphone_instance = get_microphone()
        if microphone_instance is None:
            raise HTTPException(status_code=503, detail="Microphone not available")

        with microphone_instance as source:
            # Adjust for ambient noise
            recognizer.adjust_for_ambient_noise(source, duration=0.5)
            
            # Listen for speech
            audio = recognizer.listen(source, timeout=5, phrase_time_limit=10)
            
            # Recognize speech
            try:
                text = recognizer.recognize_google(audio)
                logger.info(f"Recognized speech: {text}")
                
                # Process with NLP Engine
                nlp_result = await process_with_nlp(text)
                
                return {
                    "success": True,
                    "text": text,
                    "nlp_result": nlp_result
                }
                
            except Exception as e:
                logger.error(f"Speech recognition error: {e}")
                return {
                    "success": False,
                    "error": "Could not recognize speech"
                }
                
    except Exception as e:
        logger.error(f"Error in listen endpoint: {e}")
        raise HTTPException(status_code=500, detail=f"Listen error: {str(e)}")

@app.get("/api/voices")
async def get_available_voices():
    """
    Get available TTS voices
    """
    try:
        # TTS is temporarily disabled
        logger.warning("TTS functionality is temporarily disabled")
        return {"voices": [], "message": "TTS functionality is temporarily disabled"}
        
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
        "main:app",
        host="0.0.0.0",
        port=SPEECH_SERVICE_PORT,
        reload=True
    ) 