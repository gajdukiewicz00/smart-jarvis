"""
Use Case for Text-to-Speech processing
"""

import uuid
from typing import Optional, Dict, Any
from ...domain.services.speech_service import SpeechService
from ...domain.entities.speech_request import SpeechRequest, SpeechType
from ...domain.entities.speech_response import SpeechResponse, ResponseStatus
from ..dto.speech_dto import TextToSpeechRequest, SpeechResponseDto


class TextToSpeechUseCase:
    """
    Use Case for text-to-speech processing
    
    This use case orchestrates the business logic for text-to-speech conversion
    and follows the Single Responsibility Principle.
    """
    
    def __init__(self, speech_service: SpeechService):
        self.speech_service = speech_service
    
    async def execute(self, request_dto: TextToSpeechRequest) -> SpeechResponseDto:
        """
        Execute the use case to process text-to-speech conversion
        
        Args:
            request_dto: Text-to-speech request DTO
            
        Returns:
            SpeechResponseDto: The speech response DTO
        """
        # Validate input
        if not request_dto.text or not request_dto.text.strip():
            raise ValueError("Text cannot be empty")
        
        if not await self.speech_service.validate_text_content(request_dto.text):
            raise ValueError("Invalid text content")
        
        # Create voice settings
        voice_settings = {
            "rate": request_dto.rate,
            "volume": request_dto.volume,
            "pitch": request_dto.pitch
        }
        
        # Create speech request
        request_id = str(uuid.uuid4())
        speech_request = SpeechRequest(
            id=request_id,
            speech_type=SpeechType.TEXT_TO_SPEECH,
            content=request_dto.text,
            language=request_dto.language,
            voice_settings=voice_settings
        )
        
        # Validate request
        if not speech_request.is_valid():
            raise ValueError("Invalid speech request")
        
        # Process text-to-speech
        try:
            response = await self.speech_service.process_text_to_speech(
                text=request_dto.text,
                language=request_dto.language,
                voice_settings=voice_settings
            )
            
            # Convert to DTO
            return SpeechResponseDto(
                id=response.id,
                request_id=response.request_id,
                status=response.status.value,
                content=response.content,
                confidence=response.confidence,
                language=response.language,
                processing_time=response.processing_time,
                error_message=response.error_message,
                metadata=response.metadata,
                created_at=response.created_at
            )
            
        except Exception as e:
            # Create error response
            error_response = SpeechResponse(
                id=str(uuid.uuid4()),
                request_id=request_id,
                status=ResponseStatus.ERROR,
                content=b"",
                error_message=str(e)
            )
            
            return SpeechResponseDto(
                id=error_response.id,
                request_id=error_response.request_id,
                status=error_response.status.value,
                content=error_response.content,
                error_message=error_response.error_message,
                created_at=error_response.created_at
            )
    
    async def get_response_by_id(self, response_id: str) -> Optional[SpeechResponseDto]:
        """
        Get speech response by ID
        
        Args:
            response_id: The response ID
            
        Returns:
            Optional[SpeechResponseDto]: The speech response DTO if found
        """
        response = await self.speech_service.get_speech_response(response_id)
        
        if response is None:
            return None
        
        return SpeechResponseDto(
            id=response.id,
            request_id=response.request_id,
            status=response.status.value,
            content=response.content,
            confidence=response.confidence,
            language=response.language,
            processing_time=response.processing_time,
            error_message=response.error_message,
            metadata=response.metadata,
            created_at=response.created_at
        )
    
    async def get_available_voices(self) -> Dict[str, list]:
        """
        Get available voices
        
        Returns:
            Dict[str, list]: Available voices by language
        """
        return await self.speech_service.get_available_voices()
    
    async def get_supported_languages(self) -> list:
        """
        Get supported languages
        
        Returns:
            list: List of supported language codes
        """
        return await self.speech_service.get_supported_languages()
    
    async def get_request_by_id(self, request_id: str) -> Optional[SpeechRequest]:
        """
        Get speech request by ID
        
        Args:
            request_id: The request ID
            
        Returns:
            Optional[SpeechRequest]: The speech request if found
        """
        return await self.speech_service.get_speech_request(request_id) 