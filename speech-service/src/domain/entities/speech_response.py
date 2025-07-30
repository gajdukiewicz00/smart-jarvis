"""
Domain entity for Speech Response
"""

from dataclasses import dataclass
from datetime import datetime
from typing import Optional, Dict, Any, Union
from enum import Enum


class ResponseStatus(Enum):
    """Response status enumeration"""
    SUCCESS = "success"
    ERROR = "error"
    PROCESSING = "processing"


class ConfidenceLevel(Enum):
    """Confidence level for speech recognition"""
    HIGH = "high"
    MEDIUM = "medium"
    LOW = "low"


@dataclass
class SpeechResponse:
    """
    Domain entity representing a speech processing response
    
    This entity contains the core business logic for speech responses
    and follows the Single Responsibility Principle.
    """
    
    id: str
    request_id: str
    status: ResponseStatus
    content: Union[str, bytes]  # Text for STT, audio data for TTS
    confidence: Optional[float] = None
    language: str = "en"
    processing_time: Optional[float] = None
    error_message: Optional[str] = None
    metadata: Optional[Dict[str, Any]] = None
    created_at: Optional[datetime] = None
    
    def __post_init__(self):
        """Initialize default values after object creation"""
        if self.created_at is None:
            self.created_at = datetime.now()
        if self.metadata is None:
            self.metadata = {}
    
    def is_successful(self) -> bool:
        """
        Check if the response is successful
        
        Returns:
            bool: True if the response is successful
        """
        return self.status == ResponseStatus.SUCCESS
    
    def is_error(self) -> bool:
        """
        Check if the response has an error
        
        Returns:
            bool: True if the response has an error
        """
        return self.status == ResponseStatus.ERROR
    
    def is_processing(self) -> bool:
        """
        Check if the response is still processing
        
        Returns:
            bool: True if the response is still processing
        """
        return self.status == ResponseStatus.PROCESSING
    
    def get_confidence_level(self) -> ConfidenceLevel:
        """
        Get the confidence level based on confidence score
        
        Returns:
            ConfidenceLevel: The confidence level
        """
        if self.confidence is None:
            return ConfidenceLevel.LOW
        
        if self.confidence >= 0.8:
            return ConfidenceLevel.HIGH
        elif self.confidence >= 0.5:
            return ConfidenceLevel.MEDIUM
        else:
            return ConfidenceLevel.LOW
    
    def is_high_confidence(self) -> bool:
        """
        Check if the response has high confidence
        
        Returns:
            bool: True if confidence is high
        """
        return self.get_confidence_level() == ConfidenceLevel.HIGH
    
    def get_content_length(self) -> int:
        """
        Get the length of the content
        
        Returns:
            int: Length of the content
        """
        if isinstance(self.content, str):
            return len(self.content)
        elif isinstance(self.content, bytes):
            return len(self.content)
        return 0
    
    def add_metadata(self, key: str, value: Any):
        """
        Add metadata to the response
        
        Args:
            key: Metadata key
            value: Metadata value
        """
        self.metadata[key] = value
    
    def get_metadata(self, key: str, default: Any = None) -> Any:
        """
        Get metadata value
        
        Args:
            key: Metadata key
            default: Default value if key not found
            
        Returns:
            Any: Metadata value
        """
        return self.metadata.get(key, default)
    
    def set_processing_time(self, time_seconds: float):
        """
        Set the processing time
        
        Args:
            time_seconds: Processing time in seconds
        """
        self.processing_time = time_seconds
    
    def get_processing_time_ms(self) -> Optional[int]:
        """
        Get processing time in milliseconds
        
        Returns:
            Optional[int]: Processing time in milliseconds
        """
        if self.processing_time is not None:
            return int(self.processing_time * 1000)
        return None
    
    def set_error(self, error_message: str):
        """
        Set error status and message
        
        Args:
            error_message: Error message
        """
        self.status = ResponseStatus.ERROR
        self.error_message = error_message
    
    def set_success(self, content: Union[str, bytes], confidence: Optional[float] = None):
        """
        Set success status and content
        
        Args:
            content: Response content
            confidence: Confidence score (optional)
        """
        self.status = ResponseStatus.SUCCESS
        self.content = content
        if confidence is not None:
            self.confidence = confidence
    
    def to_dict(self) -> Dict[str, Any]:
        """
        Convert response to dictionary
        
        Returns:
            Dict[str, Any]: Dictionary representation
        """
        return {
            "id": self.id,
            "request_id": self.request_id,
            "status": self.status.value,
            "content": self.content if isinstance(self.content, str) else "[BINARY_DATA]",
            "confidence": self.confidence,
            "language": self.language,
            "processing_time": self.processing_time,
            "error_message": self.error_message,
            "metadata": self.metadata,
            "created_at": self.created_at.isoformat() if self.created_at else None
        }
    
    def __str__(self) -> str:
        """String representation of the speech response"""
        return f"SpeechResponse(id={self.id}, status={self.status.value}, language={self.language})"
    
    def __repr__(self) -> str:
        """Detailed string representation"""
        return (f"SpeechResponse(id='{self.id}', request_id='{self.request_id}', "
                f"status={self.status}, language='{self.language}')") 