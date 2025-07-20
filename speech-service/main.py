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

from speech_recognition import Recognizer, Microphone
import pyttsx3
import whisper
import tempfile
import os

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

# Initialize speech components
recognizer = Recognizer()
microphone = Microphone()
whisper_model = None
tts_engine = None

def initialize_speech_components():
    """Initialize speech recognition and TTS components"""
    global whisper_model, tts_engine
    
    try:
        # Initialize Whisper model
        logger.info("Loading Whisper model...")
        whisper_model = whisper.load_model("base")
        logger.info("Whisper model loaded successfully")
        
        # Initialize TTS engine
        logger.info("Initializing TTS engine...")
        tts_engine = pyttsx3.init()
        
        # Configure TTS engine
        voices = tts_engine.getProperty('voices')
        if voices:
            tts_engine.setProperty('voice', voices[0].id)
        
        tts_engine.setProperty('rate', 150)
        tts_engine.setProperty('volume', 1.0)
        logger.info("TTS engine initialized successfully")
        
    except Exception as e:
        logger.error(f"Error initializing speech components: {e}")
        raise

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
        timestamp=asyncio.get_event_loop().time().__str__()
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
        
        # Configure TTS engine with request parameters
        if request.voice != "default":
            voices = tts_engine.getProperty('voices')
            for voice in voices:
                if request.voice in voice.name:
                    tts_engine.setProperty('voice', voice.id)
                    break
        
        tts_engine.setProperty('rate', request.rate)
        tts_engine.setProperty('volume', request.volume)
        
        # Generate speech
        tts_engine.say(request.text)
        tts_engine.runAndWait()
        
        logger.info("Text-to-speech completed successfully")
        return {"success": True, "message": "Speech generated successfully"}
        
    except Exception as e:
        logger.error(f"Error in text-to-speech: {e}")
        raise HTTPException(status_code=500, detail=f"Text-to-speech error: {str(e)}")

@app.post("/api/listen")
async def listen_for_speech():
    """
    Listen for speech input using microphone
    """
    try:
        logger.info("Starting speech recognition...")
        
        with microphone as source:
            # Adjust for ambient noise
            recognizer.adjust_for_ambient_noise(source, duration=0.5)
            
            # Listen for speech
            audio = recognizer.listen(source, timeout=5, phrase_time_limit=10)
            
            # Recognize speech
            try:
                text = recognizer.recognize_google(audio)
                confidence = 0.8  # Google doesn't provide confidence
                
                response = SpeechToTextResponse(
                    text=text,
                    confidence=confidence,
                    language="en"
                )
                
                logger.info(f"Speech recognized: {text}")
                return response
                
            except Exception as e:
                logger.warning(f"Could not recognize speech: {e}")
                raise HTTPException(status_code=400, detail="Could not recognize speech")
                
    except Exception as e:
        logger.error(f"Error in speech recognition: {e}")
        raise HTTPException(status_code=500, detail=f"Speech recognition error: {str(e)}")

@app.get("/api/voices")
async def get_available_voices():
    """
    Get available TTS voices
    """
    try:
        voices = tts_engine.getProperty('voices')
        voice_list = []
        
        for voice in voices:
            voice_list.append({
                "id": voice.id,
                "name": voice.name,
                "languages": voice.languages
            })
        
        return {"voices": voice_list}
        
    except Exception as e:
        logger.error(f"Error getting voices: {e}")
        raise HTTPException(status_code=500, detail=f"Error getting voices: {str(e)}")

if __name__ == "__main__":
    # Run the application
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8083,
        reload=True,
        log_level="info"
    ) 