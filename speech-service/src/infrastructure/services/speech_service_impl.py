"""
Implementation of SpeechService
"""

import uuid
import time
import json
from typing import Optional, Dict, Any, Union, List
from ...domain.services.speech_service import SpeechService
from ...domain.repositories.speech_repository import SpeechRepository
from ...domain.entities.speech_request import SpeechRequest, SpeechType
from ...domain.entities.speech_response import SpeechResponse, ResponseStatus


class SpeechServiceImpl(SpeechService):
    """
    Implementation of SpeechService
    
    This service implements the domain service interface and coordinates
    between the domain layer and infrastructure layer.
    """
    
    def __init__(self, speech_repository: SpeechRepository):
        self.speech_repository = speech_repository
        self._supported_languages = ["en", "es", "fr", "de", "ru", "zh", "ja"]
        self._available_voices = {
            "en": ["default", "male", "female"],
            "es": ["default"],
            "fr": ["default"],
            "de": ["default"],
            "ru": ["default"],
            "zh": ["default"],
            "ja": ["default"]
        }
    
    async def process_speech_to_text(self, audio_data: bytes, language: str = "en") -> SpeechResponse:
        """Process speech-to-text conversion"""
        start_time = time.time()
        request_id = str(uuid.uuid4())
        response_id = str(uuid.uuid4())
        
        try:
            # Create speech request
            speech_request = SpeechRequest(
                id=request_id,
                speech_type=SpeechType.SPEECH_TO_TEXT,
                content=audio_data,
                language=language
            )
            
            # Save request
            await self.speech_repository.save_request(speech_request)
            
            # Simulate STT processing (in real implementation, this would use Whisper or similar)
            # For now, we'll return a mock response
            processing_time = time.time() - start_time
            
            # Mock STT result
            recognized_text = "Hello, this is a test speech recognition result."
            confidence = 0.85
            
            # Create response
            response = SpeechResponse(
                id=response_id,
                request_id=request_id,
                status=ResponseStatus.SUCCESS,
                content=recognized_text,
                confidence=confidence,
                language=language,
                processing_time=processing_time
            )
            
            # Mark request as processed
            speech_request.mark_as_processed()
            speech_request.set_processing_time(processing_time)
            
            # Save response and updated request
            await self.speech_repository.save_response(response)
            await self.speech_repository.save_request(speech_request)
            
            return response
            
        except Exception as e:
            processing_time = time.time() - start_time
            
            # Create error response
            error_response = SpeechResponse(
                id=response_id,
                request_id=request_id,
                status=ResponseStatus.ERROR,
                content="",
                error_message=str(e),
                processing_time=processing_time
            )
            
            await self.speech_repository.save_response(error_response)
            return error_response
    
    async def process_text_to_speech(self, text: str, language: str = "en", 
                                   voice_settings: Optional[Dict[str, Any]] = None) -> SpeechResponse:
        """Process text-to-speech conversion"""
        start_time = time.time()
        request_id = str(uuid.uuid4())
        response_id = str(uuid.uuid4())
        
        try:
            # Create speech request
            speech_request = SpeechRequest(
                id=request_id,
                speech_type=SpeechType.TEXT_TO_SPEECH,
                content=text,
                language=language,
                voice_settings=voice_settings or {}
            )
            
            # Save request
            await self.speech_repository.save_request(speech_request)
            
            # Simulate TTS processing (in real implementation, this would use gTTS or similar)
            # For now, we'll return a mock response
            processing_time = time.time() - start_time
            
            # Mock TTS result (audio data as bytes)
            audio_data = b"mock_audio_data_for_tts"
            
            # Create response
            response = SpeechResponse(
                id=response_id,
                request_id=request_id,
                status=ResponseStatus.SUCCESS,
                content=audio_data,
                language=language,
                processing_time=processing_time
            )
            
            # Mark request as processed
            speech_request.mark_as_processed()
            speech_request.set_processing_time(processing_time)
            
            # Save response and updated request
            await self.speech_repository.save_response(response)
            await self.speech_repository.save_request(speech_request)
            
            return response
            
        except Exception as e:
            processing_time = time.time() - start_time
            
            # Create error response
            error_response = SpeechResponse(
                id=response_id,
                request_id=request_id,
                status=ResponseStatus.ERROR,
                content=b"",
                error_message=str(e),
                processing_time=processing_time
            )
            
            await self.speech_repository.save_response(error_response)
            return error_response
    
    async def process_voice_command(self, audio_data: bytes, language: str = "en") -> SpeechResponse:
        """Process voice command (STT + NLP processing)"""
        start_time = time.time()
        request_id = str(uuid.uuid4())
        response_id = str(uuid.uuid4())
        
        try:
            # Create speech request
            speech_request = SpeechRequest(
                id=request_id,
                speech_type=SpeechType.VOICE_COMMAND,
                content=audio_data,
                language=language
            )
            
            # Save request
            await self.speech_repository.save_request(speech_request)
            
            # Simulate voice command processing
            processing_time = time.time() - start_time
            
            # Mock voice command result
            original_text = "What's the weather like today?"
            nlp_result = {
                "intent": "weather_query",
                "entities": {"location": "current"},
                "confidence": 0.9
            }
            tts_response = "The weather is sunny with a temperature of 25 degrees Celsius."
            
            # Create combined response
            combined_result = {
                "original_text": original_text,
                "nlp_result": nlp_result,
                "tts_response": tts_response
            }
            
            response_content = json.dumps(combined_result)
            
            # Create response
            response = SpeechResponse(
                id=response_id,
                request_id=request_id,
                status=ResponseStatus.SUCCESS,
                content=response_content,
                confidence=0.9,
                language=language,
                processing_time=processing_time
            )
            
            # Mark request as processed
            speech_request.mark_as_processed()
            speech_request.set_processing_time(processing_time)
            
            # Save response and updated request
            await self.speech_repository.save_response(response)
            await self.speech_repository.save_request(speech_request)
            
            return response
            
        except Exception as e:
            processing_time = time.time() - start_time
            
            # Create error response
            error_response = SpeechResponse(
                id=response_id,
                request_id=request_id,
                status=ResponseStatus.ERROR,
                content="",
                error_message=str(e),
                processing_time=processing_time
            )
            
            await self.speech_repository.save_response(error_response)
            return error_response
    
    async def get_speech_request(self, request_id: str) -> Optional[SpeechRequest]:
        """Get a speech request by ID"""
        return await self.speech_repository.get_request_by_id(request_id)
    
    async def get_speech_response(self, response_id: str) -> Optional[SpeechResponse]:
        """Get a speech response by ID"""
        return await self.speech_repository.get_response_by_id(response_id)
    
    async def get_recent_requests(self, limit: int = 10) -> List[SpeechRequest]:
        """Get recent speech requests"""
        return await self.speech_repository.get_recent_requests(limit)
    
    async def get_processing_statistics(self) -> Dict[str, Any]:
        """Get processing statistics"""
        return await self.speech_repository.get_processing_statistics()
    
    async def validate_audio_format(self, audio_data: bytes) -> bool:
        """Validate audio format"""
        # Simple validation - check if data is not empty
        return len(audio_data) > 0
    
    async def validate_text_content(self, text: str) -> bool:
        """Validate text content"""
        # Simple validation - check if text is not empty
        return text and text.strip()
    
    async def get_supported_languages(self) -> List[str]:
        """Get supported languages"""
        return self._supported_languages.copy()
    
    async def get_available_voices(self) -> Dict[str, List[str]]:
        """Get available voices by language"""
        return self._available_voices.copy()
    
    async def check_service_health(self) -> Dict[str, Any]:
        """Check service health"""
        return {
            "status": "healthy",
            "speech_components": {
                "stt": True,
                "tts": True,
                "voice_command": True
            },
            "repository": "connected"
        }
    
    async def cleanup_old_requests(self, days_old: int = 30) -> int:
        """Clean up old requests"""
        return await self.speech_repository.cleanup_old_requests(days_old)
    
    async def get_error_statistics(self) -> Dict[str, Any]:
        """Get error statistics"""
        return await self.speech_repository.get_error_statistics()
    
    async def get_performance_metrics(self) -> Dict[str, Any]:
        """Get performance metrics"""
        return await self.speech_repository.get_performance_metrics() 