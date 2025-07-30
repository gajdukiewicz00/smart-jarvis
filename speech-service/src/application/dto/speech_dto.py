"""
Data Transfer Objects for Speech Service
"""

from dataclasses import dataclass
from datetime import datetime
from typing import Optional, Dict, Any, Union
from pydantic import BaseModel, Field


class SpeechToTextRequest(BaseModel):
    """DTO for speech-to-text request"""
    language: str = Field(default="en", description="Language code")
    audio_format: Optional[str] = Field(default=None, description="Audio format")
    confidence_threshold: Optional[float] = Field(default=0.5, description="Minimum confidence threshold")


class TextToSpeechRequest(BaseModel):
    """DTO for text-to-speech request"""
    text: str = Field(..., description="Text to convert to speech")
    language: str = Field(default="en", description="Language code")
    voice: Optional[str] = Field(default=None, description="Voice to use")
    rate: Optional[int] = Field(default=150, description="Speech rate (words per minute)")
    volume: Optional[float] = Field(default=1.0, description="Volume level (0.0 to 1.0)")
    pitch: Optional[float] = Field(default=1.0, description="Pitch multiplier")


class VoiceCommandRequest(BaseModel):
    """DTO for voice command request"""
    language: str = Field(default="en", description="Language code")
    audio_format: Optional[str] = Field(default=None, description="Audio format")
    nlp_engine_url: Optional[str] = Field(default=None, description="NLP engine URL")


class SpeechResponseDto(BaseModel):
    """DTO for speech response"""
    id: str = Field(..., description="Response ID")
    request_id: str = Field(..., description="Request ID")
    status: str = Field(..., description="Response status")
    content: Union[str, bytes] = Field(..., description="Response content")
    confidence: Optional[float] = Field(default=None, description="Confidence score")
    language: str = Field(default="en", description="Language code")
    processing_time: Optional[float] = Field(default=None, description="Processing time in seconds")
    error_message: Optional[str] = Field(default=None, description="Error message")
    metadata: Optional[Dict[str, Any]] = Field(default=None, description="Additional metadata")
    created_at: Optional[datetime] = Field(default=None, description="Creation timestamp")


class HealthResponseDto(BaseModel):
    """DTO for health check response"""
    status: str = Field(..., description="Service status")
    service: str = Field(default="speech-service", description="Service name")
    timestamp: str = Field(..., description="Timestamp")
    nlp_engine_url: Optional[str] = Field(default=None, description="NLP engine URL")
    speech_components: Dict[str, bool] = Field(default_factory=dict, description="Speech components status")


class VoiceCommandResponseDto(BaseModel):
    """DTO for voice command response"""
    original_text: str = Field(..., description="Original recognized text")
    nlp_result: Dict[str, Any] = Field(..., description="NLP processing result")
    tts_response: str = Field(..., description="Text-to-speech response")


class ProcessingStatisticsDto(BaseModel):
    """DTO for processing statistics"""
    total_requests: int = Field(..., description="Total number of requests")
    successful_requests: int = Field(..., description="Number of successful requests")
    failed_requests: int = Field(..., description="Number of failed requests")
    average_processing_time: float = Field(..., description="Average processing time in seconds")
    requests_by_language: Dict[str, int] = Field(default_factory=dict, description="Requests by language")
    requests_by_type: Dict[str, int] = Field(default_factory=dict, description="Requests by type")


class AvailableVoicesDto(BaseModel):
    """DTO for available voices"""
    voices_by_language: Dict[str, list] = Field(..., description="Available voices by language")
    default_voice: str = Field(..., description="Default voice")
    supported_languages: list = Field(..., description="Supported languages")


class ErrorStatisticsDto(BaseModel):
    """DTO for error statistics"""
    total_errors: int = Field(..., description="Total number of errors")
    errors_by_type: Dict[str, int] = Field(default_factory=dict, description="Errors by type")
    recent_errors: list = Field(default_factory=list, description="Recent errors")
    error_rate: float = Field(..., description="Error rate percentage")


class PerformanceMetricsDto(BaseModel):
    """DTO for performance metrics"""
    average_response_time: float = Field(..., description="Average response time in seconds")
    requests_per_minute: float = Field(..., description="Requests per minute")
    memory_usage: Dict[str, float] = Field(..., description="Memory usage statistics")
    cpu_usage: float = Field(..., description="CPU usage percentage")
    active_connections: int = Field(..., description="Number of active connections")


@dataclass
class SpeechRequestDto:
    """Internal DTO for speech request"""
    id: str
    speech_type: str
    content: Union[str, bytes]
    language: str = "en"
    audio_format: Optional[str] = None
    voice_settings: Optional[Dict[str, Any]] = None
    created_at: Optional[datetime] = None
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary"""
        return {
            "id": self.id,
            "speech_type": self.speech_type,
            "content": self.content if isinstance(self.content, str) else "[BINARY_DATA]",
            "language": self.language,
            "audio_format": self.audio_format,
            "voice_settings": self.voice_settings,
            "created_at": self.created_at.isoformat() if self.created_at else None
        }


@dataclass
class SpeechResponseDto:
    """Internal DTO for speech response"""
    id: str
    request_id: str
    status: str
    content: Union[str, bytes]
    confidence: Optional[float] = None
    language: str = "en"
    processing_time: Optional[float] = None
    error_message: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None
    created_at: Optional[datetime] = None
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert to dictionary"""
        return {
            "id": self.id,
            "request_id": self.request_id,
            "status": self.status,
            "content": self.content if isinstance(self.content, str) else "[BINARY_DATA]",
            "confidence": self.confidence,
            "language": self.language,
            "processing_time": self.processing_time,
            "error_message": self.error_message,
            "metadata": self.metadata,
            "created_at": self.created_at.isoformat() if self.created_at else None
        } 