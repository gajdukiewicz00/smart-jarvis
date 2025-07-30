"""
Repository interface for Speech domain entity
"""

from abc import ABC, abstractmethod
from typing import Optional, List, Dict, Any
from ..entities.speech_request import SpeechRequest
from ..entities.speech_response import SpeechResponse


class SpeechRepository(ABC):
    """
    Repository interface for Speech domain entities
    
    This interface defines the contract for data access operations
    and follows the Dependency Inversion Principle.
    """
    
    @abstractmethod
    async def save_request(self, request: SpeechRequest) -> SpeechRequest:
        """
        Save a speech request
        
        Args:
            request: The speech request to save
            
        Returns:
            SpeechRequest: The saved speech request
        """
        pass
    
    @abstractmethod
    async def save_response(self, response: SpeechResponse) -> SpeechResponse:
        """
        Save a speech response
        
        Args:
            response: The speech response to save
            
        Returns:
            SpeechResponse: The saved speech response
        """
        pass
    
    @abstractmethod
    async def get_request_by_id(self, request_id: str) -> Optional[SpeechRequest]:
        """
        Get a speech request by ID
        
        Args:
            request_id: The request ID
            
        Returns:
            Optional[SpeechRequest]: The speech request if found
        """
        pass
    
    @abstractmethod
    async def get_response_by_id(self, response_id: str) -> Optional[SpeechResponse]:
        """
        Get a speech response by ID
        
        Args:
            response_id: The response ID
            
        Returns:
            Optional[SpeechResponse]: The speech response if found
        """
        pass
    
    @abstractmethod
    async def get_responses_by_request_id(self, request_id: str) -> List[SpeechResponse]:
        """
        Get all responses for a specific request
        
        Args:
            request_id: The request ID
            
        Returns:
            List[SpeechResponse]: List of responses for the request
        """
        pass
    
    @abstractmethod
    async def get_requests_by_status(self, status: str) -> List[SpeechRequest]:
        """
        Get requests by processing status
        
        Args:
            status: The status to filter by
            
        Returns:
            List[SpeechRequest]: List of requests with the specified status
        """
        pass
    
    @abstractmethod
    async def get_requests_by_language(self, language: str) -> List[SpeechRequest]:
        """
        Get requests by language
        
        Args:
            language: The language to filter by
            
        Returns:
            List[SpeechRequest]: List of requests with the specified language
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
    async def delete_request(self, request_id: str) -> bool:
        """
        Delete a speech request
        
        Args:
            request_id: The request ID to delete
            
        Returns:
            bool: True if deleted successfully
        """
        pass
    
    @abstractmethod
    async def delete_response(self, response_id: str) -> bool:
        """
        Delete a speech response
        
        Args:
            response_id: The response ID to delete
            
        Returns:
            bool: True if deleted successfully
        """
        pass
    
    @abstractmethod
    async def exists_request(self, request_id: str) -> bool:
        """
        Check if a request exists
        
        Args:
            request_id: The request ID to check
            
        Returns:
            bool: True if the request exists
        """
        pass
    
    @abstractmethod
    async def exists_response(self, response_id: str) -> bool:
        """
        Check if a response exists
        
        Args:
            response_id: The response ID to check
            
        Returns:
            bool: True if the response exists
        """
        pass
    
    @abstractmethod
    async def count_requests(self) -> int:
        """
        Get total number of requests
        
        Returns:
            int: Total number of requests
        """
        pass
    
    @abstractmethod
    async def count_responses(self) -> int:
        """
        Get total number of responses
        
        Returns:
            int: Total number of responses
        """
        pass 