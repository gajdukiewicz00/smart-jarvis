"""
Domain service for Speech business logic
"""

from abc import ABC, abstractmethod
from typing import Optional, Dict, Any, Union, List
from ..entities.speech_request import SpeechRequest, SpeechType
from ..entities.speech_response import SpeechResponse, ResponseStatus
from ..repositories.speech_repository import SpeechRepository


class SpeechService(ABC):
    """
    Domain service for Speech business logic
    
    This service contains business rules and operations that don't belong
    to individual entities but are part of the domain logic.
    """
    
    @abstractmethod
    async def process_speech_to_text(self, audio_data: bytes, language: str = "en") -> SpeechResponse:
        """
        Process speech-to-text conversion
        
        Args:
            audio_data: Audio data to process
            language: Language code
            
        Returns:
            SpeechResponse: The speech response
        """
        pass
    
    @abstractmethod
    async def process_text_to_speech(self, text: str, language: str = "en", 
                                   voice_settings: Optional[Dict[str, Any]] = None) -> SpeechResponse:
        """
        Process text-to-speech conversion
        
        Args:
            text: Text to convert to speech
            language: Language code
            voice_settings: Voice settings (rate, volume, pitch)
            
        Returns:
            SpeechResponse: The speech response
        """
        pass
    
    @abstractmethod
    async def process_voice_command(self, audio_data: bytes, language: str = "en") -> SpeechResponse:
        """
        Process voice command (STT + NLP processing)
        
        Args:
            audio_data: Audio data to process
            language: Language code
            
        Returns:
            SpeechResponse: The speech response with NLP results
        """
        pass
    
    @abstractmethod
    async def get_speech_request(self, request_id: str) -> Optional[SpeechRequest]:
        """
        Get a speech request by ID
        
        Args:
            request_id: The request ID
            
        Returns:
            Optional[SpeechRequest]: The speech request if found
        """
        pass
    
    @abstractmethod
    async def get_speech_response(self, response_id: str) -> Optional[SpeechResponse]:
        """
        Get a speech response by ID
        
        Args:
            response_id: The response ID
            
        Returns:
            Optional[SpeechResponse]: The speech response if found
        """
        pass
    
    @abstractmethod
    async def get_recent_requests(self, limit: int = 10) -> List[SpeechRequest]:
        """
        Get recent speech requests
        
        Args:
            limit: Maximum number of requests to return
            
        Returns:
            List[SpeechRequest]: List of recent requests
        """
        pass
    
    @abstractmethod
    async def get_processing_statistics(self) -> Dict[str, Any]:
        """
        Get processing statistics
        
        Returns:
            Dict[str, Any]: Processing statistics
        """
        pass
    
    @abstractmethod
    async def validate_audio_format(self, audio_data: bytes) -> bool:
        """
        Validate audio format
        
        Args:
            audio_data: Audio data to validate
            
        Returns:
            bool: True if audio format is valid
        """
        pass
    
    @abstractmethod
    async def validate_text_content(self, text: str) -> bool:
        """
        Validate text content
        
        Args:
            text: Text to validate
            
        Returns:
            bool: True if text content is valid
        """
        pass
    
    @abstractmethod
    async def get_supported_languages(self) -> List[str]:
        """
        Get supported languages
        
        Returns:
            List[str]: List of supported language codes
        """
        pass
    
    @abstractmethod
    async def get_available_voices(self) -> Dict[str, List[str]]:
        """
        Get available voices by language
        
        Returns:
            Dict[str, List[str]]: Available voices by language
        """
        pass
    
    @abstractmethod
    async def check_service_health(self) -> Dict[str, Any]:
        """
        Check service health
        
        Returns:
            Dict[str, Any]: Health status information
        """
        pass
    
    @abstractmethod
    async def cleanup_old_requests(self, days_old: int = 30) -> int:
        """
        Clean up old requests
        
        Args:
            days_old: Number of days old to consider for cleanup
            
        Returns:
            int: Number of requests cleaned up
        """
        pass
    
    @abstractmethod
    async def get_error_statistics(self) -> Dict[str, Any]:
        """
        Get error statistics
        
        Returns:
            Dict[str, Any]: Error statistics
        """
        pass
    
    @abstractmethod
    async def get_performance_metrics(self) -> Dict[str, Any]:
        """
        Get performance metrics
        
        Returns:
            Dict[str, Any]: Performance metrics
        """
        pass 