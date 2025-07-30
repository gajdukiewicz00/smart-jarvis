"""
Unit tests for Speech domain entities
"""

import pytest
from datetime import datetime, timedelta
from src.domain.entities.speech_request import SpeechRequest, SpeechType, AudioFormat
from src.domain.entities.speech_response import SpeechResponse, ResponseStatus, ConfidenceLevel


class TestSpeechRequest:
    """Test cases for SpeechRequest entity"""
    
    def test_speech_request_creation(self):
        """Test speech request creation"""
        request = SpeechRequest(
            id="test-id",
            speech_type=SpeechType.SPEECH_TO_TEXT,
            content=b"audio_data",
            language="en"
        )
        
        assert request.id == "test-id"
        assert request.speech_type == SpeechType.SPEECH_TO_TEXT
        assert request.content == b"audio_data"
        assert request.language == "en"
        assert request.created_at is not None
        assert request.voice_settings is not None
    
    def test_speech_request_validation_stt(self):
        """Test speech request validation for STT"""
        # Valid STT request
        valid_request = SpeechRequest(
            id="test-id",
            speech_type=SpeechType.SPEECH_TO_TEXT,
            content=b"audio_data",
            language="en"
        )
        assert valid_request.is_valid() is True
        
        # Invalid STT request - empty content
        invalid_request = SpeechRequest(
            id="test-id",
            speech_type=SpeechType.SPEECH_TO_TEXT,
            content=b"",
            language="en"
        )
        assert invalid_request.is_valid() is False
    
    def test_speech_request_validation_tts(self):
        """Test speech request validation for TTS"""
        # Valid TTS request
        valid_request = SpeechRequest(
            id="test-id",
            speech_type=SpeechType.TEXT_TO_SPEECH,
            content="Hello world",
            language="en"
        )
        assert valid_request.is_valid() is True
        
        # Invalid TTS request - empty text
        invalid_request = SpeechRequest(
            id="test-id",
            speech_type=SpeechType.TEXT_TO_SPEECH,
            content="",
            language="en"
        )
        assert invalid_request.is_valid() is False
    
    def test_speech_request_processing_status(self):
        """Test speech request processing status"""
        request = SpeechRequest(
            id="test-id",
            speech_type=SpeechType.SPEECH_TO_TEXT,
            content=b"audio_data",
            language="en"
        )
        
        # Initially not processed
        assert request.is_processed() is False
        assert request.get_processing_time() is None
        
        # Mark as processed
        request.mark_as_processed()
        assert request.is_processed() is True
        assert request.get_processing_time() is not None
    
    def test_speech_request_voice_settings(self):
        """Test speech request voice settings"""
        request = SpeechRequest(
            id="test-id",
            speech_type=SpeechType.TEXT_TO_SPEECH,
            content="Hello world",
            language="en"
        )
        
        # Default voice settings
        assert request.get_voice_rate() == 150
        assert request.get_voice_volume() == 1.0
        assert request.get_voice_pitch() == 1.0
        
        # Update voice settings
        request.update_voice_settings({
            "rate": 200,
            "volume": 0.8,
            "pitch": 1.2
        })
        
        assert request.get_voice_rate() == 200
        assert request.get_voice_volume() == 0.8
        assert request.get_voice_pitch() == 1.2
    
    def test_speech_request_string_representation(self):
        """Test speech request string representation"""
        request = SpeechRequest(
            id="test-id",
            speech_type=SpeechType.SPEECH_TO_TEXT,
            content=b"audio_data",
            language="en"
        )
        
        str_repr = str(request)
        assert "SpeechRequest" in str_repr
        assert "test-id" in str_repr
        assert "speech_to_text" in str_repr


class TestSpeechResponse:
    """Test cases for SpeechResponse entity"""
    
    def test_speech_response_creation(self):
        """Test speech response creation"""
        response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world",
            confidence=0.9,
            language="en"
        )
        
        assert response.id == "response-id"
        assert response.request_id == "request-id"
        assert response.status == ResponseStatus.SUCCESS
        assert response.content == "Hello world"
        assert response.confidence == 0.9
        assert response.language == "en"
        assert response.created_at is not None
        assert response.metadata is not None
    
    def test_speech_response_status_checks(self):
        """Test speech response status checks"""
        # Success response
        success_response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world"
        )
        assert success_response.is_successful() is True
        assert success_response.is_error() is False
        assert success_response.is_processing() is False
        
        # Error response
        error_response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.ERROR,
            content="",
            error_message="Processing failed"
        )
        assert error_response.is_successful() is False
        assert error_response.is_error() is True
        assert error_response.is_processing() is False
    
    def test_speech_response_confidence_levels(self):
        """Test speech response confidence levels"""
        # High confidence
        high_conf_response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world",
            confidence=0.9
        )
        assert high_conf_response.get_confidence_level() == ConfidenceLevel.HIGH
        assert high_conf_response.is_high_confidence() is True
        
        # Medium confidence
        medium_conf_response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world",
            confidence=0.7
        )
        assert medium_conf_response.get_confidence_level() == ConfidenceLevel.MEDIUM
        assert medium_conf_response.is_high_confidence() is False
        
        # Low confidence
        low_conf_response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world",
            confidence=0.3
        )
        assert low_conf_response.get_confidence_level() == ConfidenceLevel.LOW
        assert low_conf_response.is_high_confidence() is False
    
    def test_speech_response_content_length(self):
        """Test speech response content length"""
        # String content
        text_response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world"
        )
        assert text_response.get_content_length() == 11
        
        # Bytes content
        audio_response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content=b"audio_data"
        )
        assert audio_response.get_content_length() == 10
    
    def test_speech_response_metadata(self):
        """Test speech response metadata operations"""
        response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world"
        )
        
        # Add metadata
        response.add_metadata("processing_time", 1.5)
        response.add_metadata("model_version", "v1.0")
        
        # Get metadata
        assert response.get_metadata("processing_time") == 1.5
        assert response.get_metadata("model_version") == "v1.0"
        assert response.get_metadata("nonexistent", "default") == "default"
    
    def test_speech_response_processing_time(self):
        """Test speech response processing time"""
        response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world"
        )
        
        # Set processing time
        response.set_processing_time(2.5)
        assert response.processing_time == 2.5
        assert response.get_processing_time_ms() == 2500
    
    def test_speech_response_error_handling(self):
        """Test speech response error handling"""
        response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world"
        )
        
        # Set error
        response.set_error("Processing failed")
        assert response.is_error() is True
        assert response.error_message == "Processing failed"
    
    def test_speech_response_success_handling(self):
        """Test speech response success handling"""
        response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.ERROR,
            content="",
            error_message="Processing failed"
        )
        
        # Set success
        response.set_success("Hello world", 0.9)
        assert response.is_successful() is True
        assert response.content == "Hello world"
        assert response.confidence == 0.9
    
    def test_speech_response_to_dict(self):
        """Test speech response to dictionary conversion"""
        response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world",
            confidence=0.9,
            language="en"
        )
        
        response_dict = response.to_dict()
        assert response_dict["id"] == "response-id"
        assert response_dict["request_id"] == "request-id"
        assert response_dict["status"] == "success"
        assert response_dict["content"] == "Hello world"
        assert response_dict["confidence"] == 0.9
        assert response_dict["language"] == "en"
    
    def test_speech_response_string_representation(self):
        """Test speech response string representation"""
        response = SpeechResponse(
            id="response-id",
            request_id="request-id",
            status=ResponseStatus.SUCCESS,
            content="Hello world"
        )
        
        str_repr = str(response)
        assert "SpeechResponse" in str_repr
        assert "response-id" in str_repr
        assert "success" in str_repr 