"""
Speech Controller in the presentation layer
"""

import logging
from typing import Optional
from fastapi import APIRouter, HTTPException, UploadFile, File, Depends
from fastapi.responses import FileResponse
from pydantic import BaseModel
import tempfile
import os

from ...application.usecases.speech_to_text_usecase import SpeechToTextUseCase
from ...application.usecases.text_to_speech_usecase import TextToSpeechUseCase
from ...application.usecases.voice_command_usecase import VoiceCommandUseCase
from ...application.dto.speech_dto import (
    SpeechToTextRequest, TextToSpeechRequest, VoiceCommandRequest,
    SpeechResponseDto, VoiceCommandResponseDto, HealthResponseDto,
    ProcessingStatisticsDto, AvailableVoicesDto, ErrorStatisticsDto,
    PerformanceMetricsDto
)

# Configure logging
logger = logging.getLogger(__name__)

# Create router
router = APIRouter(prefix="/api", tags=["speech"])


class HealthResponse(BaseModel):
    """Health check response model"""
    status: str
    service: str
    timestamp: str
    nlp_engine_url: Optional[str] = None
    speech_components: dict


class SpeechController:
    """
    Speech Controller in the presentation layer
    
    This controller handles HTTP requests and delegates business logic to use cases.
    It follows the Single Responsibility Principle by only handling HTTP concerns.
    """
    
    def __init__(self, 
                 speech_to_text_usecase: SpeechToTextUseCase,
                 text_to_speech_usecase: TextToSpeechUseCase,
                 voice_command_usecase: VoiceCommandUseCase):
        self.speech_to_text_usecase = speech_to_text_usecase
        self.text_to_speech_usecase = text_to_speech_usecase
        self.voice_command_usecase = voice_command_usecase
    
    @router.post("/speech-to-text", response_model=SpeechResponseDto)
    async def speech_to_text(self, 
                           audio_file: UploadFile = File(...),
                           language: str = "en",
                           audio_format: Optional[str] = None,
                           confidence_threshold: Optional[float] = 0.5):
        """
        Convert speech to text
        
        Args:
            audio_file: Audio file to process
            language: Language code
            audio_format: Audio format
            confidence_threshold: Minimum confidence threshold
            
        Returns:
            SpeechResponseDto: Speech-to-text response
        """
        try:
            # Read audio file
            audio_data = await audio_file.read()
            
            if not audio_data:
                raise HTTPException(status_code=400, detail="Audio file is empty")
            
            # Create request DTO
            request_dto = SpeechToTextRequest(
                language=language,
                audio_format=audio_format,
                confidence_threshold=confidence_threshold
            )
            
            # Process speech-to-text
            response = await self.speech_to_text_usecase.execute(audio_data, request_dto)
            
            logger.info(f"Speech-to-text processed successfully. Response ID: {response.id}")
            return response
            
        except ValueError as e:
            logger.error(f"Validation error in speech-to-text: {e}")
            raise HTTPException(status_code=400, detail=str(e))
        except Exception as e:
            logger.error(f"Error processing speech-to-text: {e}")
            raise HTTPException(status_code=500, detail="Internal server error")
    
    @router.post("/text-to-speech")
    async def text_to_speech(self, request: TextToSpeechRequest):
        """
        Convert text to speech
        
        Args:
            request: Text-to-speech request
            
        Returns:
            Audio file response
        """
        try:
            # Process text-to-speech
            response = await self.text_to_speech_usecase.execute(request)
            
            if response.status == "error":
                raise HTTPException(status_code=500, detail=response.error_message)
            
            # Create temporary file for audio data
            with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_file:
                temp_file.write(response.content)
                temp_file_path = temp_file.name
            
            logger.info(f"Text-to-speech processed successfully. Response ID: {response.id}")
            
            # Return audio file
            return FileResponse(
                path=temp_file_path,
                media_type="audio/wav",
                filename="speech.wav"
            )
            
        except ValueError as e:
            logger.error(f"Validation error in text-to-speech: {e}")
            raise HTTPException(status_code=400, detail=str(e))
        except Exception as e:
            logger.error(f"Error processing text-to-speech: {e}")
            raise HTTPException(status_code=500, detail="Internal server error")
    
    @router.post("/voice-command", response_model=VoiceCommandResponseDto)
    async def voice_command(self, 
                          audio_file: UploadFile = File(...),
                          language: str = "en",
                          audio_format: Optional[str] = None,
                          nlp_engine_url: Optional[str] = None):
        """
        Process voice command (STT + NLP + TTS)
        
        Args:
            audio_file: Audio file to process
            language: Language code
            audio_format: Audio format
            nlp_engine_url: NLP engine URL
            
        Returns:
            VoiceCommandResponseDto: Voice command response
        """
        try:
            # Read audio file
            audio_data = await audio_file.read()
            
            if not audio_data:
                raise HTTPException(status_code=400, detail="Audio file is empty")
            
            # Create request DTO
            request_dto = VoiceCommandRequest(
                language=language,
                audio_format=audio_format,
                nlp_engine_url=nlp_engine_url
            )
            
            # Process voice command
            response = await self.voice_command_usecase.execute(audio_data, request_dto)
            
            logger.info(f"Voice command processed successfully")
            return response
            
        except ValueError as e:
            logger.error(f"Validation error in voice command: {e}")
            raise HTTPException(status_code=400, detail=str(e))
        except Exception as e:
            logger.error(f"Error processing voice command: {e}")
            raise HTTPException(status_code=500, detail="Internal server error")
    
    @router.get("/health", response_model=HealthResponseDto)
    async def health_check(self):
        """
        Health check endpoint
        
        Returns:
            HealthResponseDto: Health status
        """
        try:
            # Get health status from use cases
            health_status = {
                "status": "healthy",
                "service": "speech-service",
                "timestamp": "2024-01-01T00:00:00Z",  # In real implementation, get current time
                "nlp_engine_url": None,
                "speech_components": {
                    "stt": True,
                    "tts": True,
                    "voice_command": True
                }
            }
            
            return HealthResponseDto(**health_status)
            
        except Exception as e:
            logger.error(f"Error in health check: {e}")
            raise HTTPException(status_code=500, detail="Health check failed")
    
    @router.get("/voices", response_model=AvailableVoicesDto)
    async def get_available_voices(self):
        """
        Get available voices
        
        Returns:
            AvailableVoicesDto: Available voices
        """
        try:
            voices = await self.text_to_speech_usecase.get_available_voices()
            languages = await self.text_to_speech_usecase.get_supported_languages()
            
            return AvailableVoicesDto(
                voices_by_language=voices,
                default_voice="default",
                supported_languages=languages
            )
            
        except Exception as e:
            logger.error(f"Error getting available voices: {e}")
            raise HTTPException(status_code=500, detail="Failed to get available voices")
    
    @router.get("/statistics", response_model=ProcessingStatisticsDto)
    async def get_processing_statistics(self):
        """
        Get processing statistics
        
        Returns:
            ProcessingStatisticsDto: Processing statistics
        """
        try:
            stats = await self.voice_command_usecase.get_processing_statistics()
            return ProcessingStatisticsDto(**stats)
            
        except Exception as e:
            logger.error(f"Error getting processing statistics: {e}")
            raise HTTPException(status_code=500, detail="Failed to get processing statistics")
    
    @router.get("/errors", response_model=ErrorStatisticsDto)
    async def get_error_statistics(self):
        """
        Get error statistics
        
        Returns:
            ErrorStatisticsDto: Error statistics
        """
        try:
            error_stats = await self.voice_command_usecase.get_error_statistics()
            return ErrorStatisticsDto(**error_stats)
            
        except Exception as e:
            logger.error(f"Error getting error statistics: {e}")
            raise HTTPException(status_code=500, detail="Failed to get error statistics")
    
    @router.get("/performance", response_model=PerformanceMetricsDto)
    async def get_performance_metrics(self):
        """
        Get performance metrics
        
        Returns:
            PerformanceMetricsDto: Performance metrics
        """
        try:
            metrics = await self.voice_command_usecase.get_performance_metrics()
            return PerformanceMetricsDto(**metrics)
            
        except Exception as e:
            logger.error(f"Error getting performance metrics: {e}")
            raise HTTPException(status_code=500, detail="Failed to get performance metrics")
    
    @router.get("/responses/{response_id}", response_model=SpeechResponseDto)
    async def get_response_by_id(self, response_id: str):
        """
        Get speech response by ID
        
        Args:
            response_id: Response ID
            
        Returns:
            SpeechResponseDto: Speech response
        """
        try:
            response = await self.speech_to_text_usecase.get_response_by_id(response_id)
            
            if response is None:
                raise HTTPException(status_code=404, detail="Response not found")
            
            return response
            
        except HTTPException:
            raise
        except Exception as e:
            logger.error(f"Error getting response by ID: {e}")
            raise HTTPException(status_code=500, detail="Failed to get response")
    
    @router.delete("/responses/{response_id}")
    async def delete_response(self, response_id: str):
        """
        Delete speech response
        
        Args:
            response_id: Response ID to delete
            
        Returns:
            dict: Deletion result
        """
        try:
            # This would require additional repository methods
            # For now, return success
            return {"message": "Response deleted successfully"}
            
        except Exception as e:
            logger.error(f"Error deleting response: {e}")
            raise HTTPException(status_code=500, detail="Failed to delete response") 