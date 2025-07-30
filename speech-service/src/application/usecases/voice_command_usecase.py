"""
Use Case for Voice Command processing
"""

import uuid
from typing import Optional, Dict, Any
from ...domain.services.speech_service import SpeechService
from ...domain.entities.speech_request import SpeechRequest, SpeechType
from ...domain.entities.speech_response import SpeechResponse, ResponseStatus
from ..dto.speech_dto import VoiceCommandRequest, VoiceCommandResponseDto, SpeechResponseDto


class VoiceCommandUseCase:
    """
    Use Case for voice command processing
    
    This use case orchestrates the business logic for voice command processing
    (STT + NLP + TTS) and follows the Single Responsibility Principle.
    """
    
    def __init__(self, speech_service: SpeechService):
        self.speech_service = speech_service
    
    async def execute(self, audio_data: bytes, request_dto: VoiceCommandRequest) -> VoiceCommandResponseDto:
        """
        Execute the use case to process voice command
        
        Args:
            audio_data: Audio data to process
            request_dto: Voice command request DTO
            
        Returns:
            VoiceCommandResponseDto: The voice command response DTO
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
            speech_type=SpeechType.VOICE_COMMAND,
            content=audio_data,
            language=request_dto.language,
            audio_format=request_dto.audio_format
        )
        
        # Validate request
        if not speech_request.is_valid():
            raise ValueError("Invalid speech request")
        
        # Process voice command
        try:
            response = await self.speech_service.process_voice_command(
                audio_data=audio_data,
                language=request_dto.language
            )
            
            # Extract components from response
            original_text = ""
            nlp_result = {}
            tts_response = ""
            
            if response.is_successful():
                # Parse response content (assuming it's a JSON string with components)
                if isinstance(response.content, str):
                    try:
                        import json
                        response_data = json.loads(response.content)
                        original_text = response_data.get("original_text", "")
                        nlp_result = response_data.get("nlp_result", {})
                        tts_response = response_data.get("tts_response", "")
                    except (json.JSONDecodeError, KeyError):
                        # Fallback: treat content as original text
                        original_text = response.content
                        nlp_result = {"status": "processed"}
                        tts_response = "Command processed successfully"
            
            return VoiceCommandResponseDto(
                original_text=original_text,
                nlp_result=nlp_result,
                tts_response=tts_response
            )
            
        except Exception as e:
            # Return error response
            return VoiceCommandResponseDto(
                original_text="",
                nlp_result={"error": str(e), "status": "error"},
                tts_response="Sorry, I couldn't process your command"
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
    
    async def get_processing_statistics(self) -> Dict[str, Any]:
        """
        Get processing statistics
        
        Returns:
            Dict[str, Any]: Processing statistics
        """
        return await self.speech_service.get_processing_statistics()
    
    async def get_error_statistics(self) -> Dict[str, Any]:
        """
        Get error statistics
        
        Returns:
            Dict[str, Any]: Error statistics
        """
        return await self.speech_service.get_error_statistics()
    
    async def get_performance_metrics(self) -> Dict[str, Any]:
        """
        Get performance metrics
        
        Returns:
            Dict[str, Any]: Performance metrics
        """
        return await self.speech_service.get_performance_metrics() 