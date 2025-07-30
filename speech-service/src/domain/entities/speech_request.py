"""
Domain entity for Speech Request
"""

from dataclasses import dataclass
from datetime import datetime
from typing import Optional, Dict, Any
from enum import Enum


class SpeechType(Enum):
    """Types of speech processing"""
    SPEECH_TO_TEXT = "speech_to_text"
    TEXT_TO_SPEECH = "text_to_speech"
    VOICE_COMMAND = "voice_command"


class AudioFormat(Enum):
    """Supported audio formats"""
    WAV = "wav"
    MP3 = "mp3"
    FLAC = "flac"
    OGG = "ogg"


@dataclass
class SpeechRequest:
    """
    Domain entity representing a speech processing request
    
    This entity contains the core business logic for speech processing
    and follows the Single Responsibility Principle.
    """
    
    id: str
    speech_type: SpeechType
    content: str  # Text for TTS, audio data for STT
    language: str = "en"
    audio_format: Optional[AudioFormat] = None
    voice_settings: Optional[Dict[str, Any]] = None
    created_at: Optional[datetime] = None
    processed_at: Optional[datetime] = None
    
    def __post_init__(self):
        """Initialize default values after object creation"""
        if self.created_at is None:
            self.created_at = datetime.now()
        if self.voice_settings is None:
            self.voice_settings = {
                "rate": 150,
                "volume": 1.0,
                "pitch": 1.0
            }
    
    def is_valid(self) -> bool:
        """
        Validate the speech request
        
        Returns:
            bool: True if the request is valid
        """
        if not self.content or not self.content.strip():
            return False
        
        if self.speech_type == SpeechType.TEXT_TO_SPEECH:
            # For TTS, content should be text
            return len(self.content.strip()) > 0
        
        elif self.speech_type == SpeechType.SPEECH_TO_TEXT:
            # For STT, content should be audio data
            return len(self.content) > 0
        
        elif self.speech_type == SpeechType.VOICE_COMMAND:
            # For voice commands, content should be audio data
            return len(self.content) > 0
        
        return False
    
    def is_processed(self) -> bool:
        """
        Check if the request has been processed
        
        Returns:
            bool: True if the request has been processed
        """
        return self.processed_at is not None
    
    def mark_as_processed(self):
        """Mark the request as processed"""
        self.processed_at = datetime.now()
    
    def get_processing_time(self) -> Optional[float]:
        """
        Get the processing time in seconds
        
        Returns:
            Optional[float]: Processing time in seconds, None if not processed
        """
        if not self.is_processed():
            return None
        
        return (self.processed_at - self.created_at).total_seconds()
    
    def update_voice_settings(self, settings: Dict[str, Any]):
        """
        Update voice settings
        
        Args:
            settings: New voice settings
        """
        if settings:
            self.voice_settings.update(settings)
    
    def get_voice_rate(self) -> int:
        """
        Get the voice rate setting
        
        Returns:
            int: Voice rate (words per minute)
        """
        return self.voice_settings.get("rate", 150)
    
    def get_voice_volume(self) -> float:
        """
        Get the voice volume setting
        
        Returns:
            float: Voice volume (0.0 to 1.0)
        """
        return self.voice_settings.get("volume", 1.0)
    
    def get_voice_pitch(self) -> float:
        """
        Get the voice pitch setting
        
        Returns:
            float: Voice pitch multiplier
        """
        return self.voice_settings.get("pitch", 1.0)
    
    def __str__(self) -> str:
        """String representation of the speech request"""
        return f"SpeechRequest(id={self.id}, type={self.speech_type.value}, language={self.language})"
    
    def __repr__(self) -> str:
        """Detailed string representation"""
        return (f"SpeechRequest(id='{self.id}', speech_type={self.speech_type}, "
                f"language='{self.language}', created_at={self.created_at})") 