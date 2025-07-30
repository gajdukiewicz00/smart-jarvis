"""
Use Case for Speech-to-Text processing
"""

import uuid
from typing import Optional
from ...domain.services.speech_service import SpeechService
from ...domain.entities.speech_request import SpeechRequest, SpeechType
from ...domain.entities.speech_response import SpeechResponse, ResponseStatus
from ..dto.speech_dto import SpeechToTextRequest, SpeechResponseDto


class SpeechToTextUseCase:
    """
    Use Case for speech-to-text processing
    
    This use case orchestrates the business logic for speech-to-text conversion
    and follows the Single Responsibility Principle.
    """
    
    def __init__(self, speech_service: SpeechService):
        self.speech_service = speech_service
    
    async def execute(self, audio_data: bytes, request_dto: SpeechToTextRequest) -> SpeechResponseDto:
        """
        Execute the use case to process speech-to-text conversion
        
        Args:
            audio_data: Audio data to process
            request_dto: Speech-to-text request DTO
            
        Returns:
            SpeechResponseDto: The speech response DTO
        """
        # Validate input
        if not audio_data:
            raise ValueError("Audio data cannot be empty")
        
        if not await self.speech_service.validate_audio_format(audio_data):
            raise ValueError("Invalid audio format")
        
        # Create speech request
        request_id = str(uuid.uuid4())
        speech_request = SpeechRequest(
            id=request_id,
            speech_type=SpeechType.SPEECH_TO_TEXT,
            content=audio_data,
            language=request_dto.language,
            audio_format=request_dto.audio_format
        )
        
        # Validate request
        if not speech_request.is_valid():
            raise ValueError("Invalid speech request")
        
        # Process speech-to-text
        try:
            response = await self.speech_service.process_speech_to_text(
                audio_data=audio_data,
                language=request_dto.language
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
                content="",
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
    
    async def get_request_by_id(self, request_id: str) -> Optional[SpeechRequest]:
        """
        Get speech request by ID
        
        Args:
            request_id: The request ID
            
        Returns:
            Optional[SpeechRequest]: The speech request if found
        """
        return await self.speech_service.get_speech_request(request_id) 