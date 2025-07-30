"""
In-memory implementation of SpeechRepository
"""

import asyncio
from typing import Optional, List, Dict, Any
from datetime import datetime, timedelta
from ...domain.repositories.speech_repository import SpeechRepository
from ...domain.entities.speech_request import SpeechRequest
from ...domain.entities.speech_response import SpeechResponse


class InMemorySpeechRepository(SpeechRepository):
    """
    In-memory implementation of SpeechRepository
    
    This implementation is used for testing and development purposes.
    It stores requests and responses in memory using dictionaries.
    """
    
    def __init__(self):
        self._requests: Dict[str, SpeechRequest] = {}
        self._responses: Dict[str, SpeechResponse] = {}
        self._lock = asyncio.Lock()
    
    async def save_request(self, request: SpeechRequest) -> SpeechRequest:
        """Save a speech request"""
        async with self._lock:
            self._requests[request.id] = request
            return request
    
    async def save_response(self, response: SpeechResponse) -> SpeechResponse:
        """Save a speech response"""
        async with self._lock:
            self._responses[response.id] = response
            return response
    
    async def get_request_by_id(self, request_id: str) -> Optional[SpeechRequest]:
        """Get a speech request by ID"""
        async with self._lock:
            return self._requests.get(request_id)
    
    async def get_response_by_id(self, response_id: str) -> Optional[SpeechResponse]:
        """Get a speech response by ID"""
        async with self._lock:
            return self._responses.get(response_id)
    
    async def get_responses_by_request_id(self, request_id: str) -> List[SpeechResponse]:
        """Get all responses for a specific request"""
        async with self._lock:
            return [
                response for response in self._responses.values()
                if response.request_id == request_id
            ]
    
    async def get_requests_by_status(self, status: str) -> List[SpeechRequest]:
        """Get requests by processing status"""
        async with self._lock:
            return [
                request for request in self._requests.values()
                if request.is_processed() == (status == "processed")
            ]
    
    async def get_requests_by_language(self, language: str) -> List[SpeechRequest]:
        """Get requests by language"""
        async with self._lock:
            return [
                request for request in self._requests.values()
                if request.language == language
            ]
    
    async def get_recent_requests(self, limit: int = 10) -> List[SpeechRequest]:
        """Get recent speech requests"""
        async with self._lock:
            sorted_requests = sorted(
                self._requests.values(),
                key=lambda x: x.created_at,
                reverse=True
            )
            return sorted_requests[:limit]
    
    async def get_processing_statistics(self) -> Dict[str, Any]:
        """Get processing statistics"""
        async with self._lock:
            total_requests = len(self._requests)
            processed_requests = len([r for r in self._requests.values() if r.is_processed()])
            total_responses = len(self._responses)
            successful_responses = len([r for r in self._responses.values() if r.is_successful()])
            
            # Calculate average processing time
            processing_times = [
                r.get_processing_time() for r in self._requests.values()
                if r.get_processing_time() is not None
            ]
            avg_processing_time = sum(processing_times) / len(processing_times) if processing_times else 0
            
            # Requests by language
            requests_by_language = {}
            for request in self._requests.values():
                lang = request.language
                requests_by_language[lang] = requests_by_language.get(lang, 0) + 1
            
            # Requests by type
            requests_by_type = {}
            for request in self._requests.values():
                req_type = request.speech_type.value
                requests_by_type[req_type] = requests_by_type.get(req_type, 0) + 1
            
            return {
                "total_requests": total_requests,
                "processed_requests": processed_requests,
                "total_responses": total_responses,
                "successful_responses": successful_responses,
                "average_processing_time": avg_processing_time,
                "requests_by_language": requests_by_language,
                "requests_by_type": requests_by_type
            }
    
    async def delete_request(self, request_id: str) -> bool:
        """Delete a speech request"""
        async with self._lock:
            if request_id in self._requests:
                del self._requests[request_id]
                return True
            return False
    
    async def delete_response(self, response_id: str) -> bool:
        """Delete a speech response"""
        async with self._lock:
            if response_id in self._responses:
                del self._responses[response_id]
                return True
            return False
    
    async def exists_request(self, request_id: str) -> bool:
        """Check if a request exists"""
        async with self._lock:
            return request_id in self._requests
    
    async def exists_response(self, response_id: str) -> bool:
        """Check if a response exists"""
        async with self._lock:
            return response_id in self._responses
    
    async def count_requests(self) -> int:
        """Get total number of requests"""
        async with self._lock:
            return len(self._requests)
    
    async def count_responses(self) -> int:
        """Get total number of responses"""
        async with self._lock:
            return len(self._responses)
    
    async def cleanup_old_requests(self, days_old: int = 30) -> int:
        """Clean up old requests"""
        async with self._lock:
            cutoff_date = datetime.now() - timedelta(days=days_old)
            old_request_ids = [
                req_id for req_id, request in self._requests.items()
                if request.created_at and request.created_at < cutoff_date
            ]
            
            for req_id in old_request_ids:
                del self._requests[req_id]
            
            return len(old_request_ids)
    
    async def get_error_statistics(self) -> Dict[str, Any]:
        """Get error statistics"""
        async with self._lock:
            error_responses = [r for r in self._responses.values() if r.is_error()]
            total_responses = len(self._responses)
            error_rate = (len(error_responses) / total_responses * 100) if total_responses > 0 else 0
            
            # Errors by type (simplified)
            errors_by_type = {"processing_error": len(error_responses)}
            
            # Recent errors
            recent_errors = [
                {
                    "id": r.id,
                    "error_message": r.error_message,
                    "created_at": r.created_at.isoformat() if r.created_at else None
                }
                for r in sorted(error_responses, key=lambda x: x.created_at, reverse=True)[:10]
            ]
            
            return {
                "total_errors": len(error_responses),
                "errors_by_type": errors_by_type,
                "recent_errors": recent_errors,
                "error_rate": error_rate
            }
    
    async def get_performance_metrics(self) -> Dict[str, Any]:
        """Get performance metrics"""
        async with self._lock:
            # Calculate average response time
            response_times = [
                r.processing_time for r in self._responses.values()
                if r.processing_time is not None
            ]
            avg_response_time = sum(response_times) / len(response_times) if response_times else 0
            
            # Calculate requests per minute (simplified)
            if self._requests:
                oldest_request = min(self._requests.values(), key=lambda x: x.created_at)
                newest_request = max(self._requests.values(), key=lambda x: x.created_at)
                
                if oldest_request.created_at and newest_request.created_at:
                    time_diff = (newest_request.created_at - oldest_request.created_at).total_seconds() / 60
                    requests_per_minute = len(self._requests) / time_diff if time_diff > 0 else 0
                else:
                    requests_per_minute = 0
            else:
                requests_per_minute = 0
            
            return {
                "average_response_time": avg_response_time,
                "requests_per_minute": requests_per_minute,
                "memory_usage": {"used": len(self._requests) + len(self._responses)},
                "cpu_usage": 0.0,  # Not implemented in in-memory version
                "active_connections": 1  # Always 1 for in-memory
            } 