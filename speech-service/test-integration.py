#!/usr/bin/env python3
"""
Test script for Speech Service integration with NLP Engine
"""

import asyncio
import httpx
import json
import os
from typing import Dict, Any

# Configuration
SPEECH_SERVICE_URL = os.getenv("SPEECH_SERVICE_URL", "http://localhost:8083")
NLP_ENGINE_URL = os.getenv("NLP_ENGINE_URL", "http://localhost:8082")

class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    ENDC = '\033[0m'

async def test_endpoint(url: str, method: str = "GET", data: Dict[str, Any] = None) -> Dict[str, Any]:
    """Test an endpoint and return the result"""
    try:
        async with httpx.AsyncClient() as client:
            if method == "GET":
                response = await client.get(url, timeout=10.0)
            elif method == "POST":
                response = await client.post(url, json=data, timeout=10.0)
            else:
                raise ValueError(f"Unsupported method: {method}")
            
            return {
                "success": response.status_code == 200,
                "status_code": response.status_code,
                "data": response.json() if response.status_code == 200 else response.text
            }
    except Exception as e:
        return {
            "success": False,
            "error": str(e)
        }

async def test_speech_service_health():
    """Test Speech Service health endpoint"""
    print(f"{Colors.BLUE}Testing Speech Service Health...{Colors.ENDC}")
    result = await test_endpoint(f"{SPEECH_SERVICE_URL}/health")
    
    if result["success"]:
        print(f"{Colors.GREEN}✅ Speech Service is healthy{Colors.ENDC}")
        print(f"   NLP Engine URL: {result['data'].get('nlp_engine_url', 'N/A')}")
    else:
        print(f"{Colors.RED}❌ Speech Service health check failed{Colors.ENDC}")
        print(f"   Error: {result.get('error', 'Unknown error')}")
    
    return result

async def test_nlp_engine_health():
    """Test NLP Engine health endpoint"""
    print(f"\n{Colors.BLUE}Testing NLP Engine Health...{Colors.ENDC}")
    result = await test_endpoint(f"{NLP_ENGINE_URL}/health")
    
    if result["success"]:
        print(f"{Colors.GREEN}✅ NLP Engine is healthy{Colors.ENDC}")
    else:
        print(f"{Colors.RED}❌ NLP Engine health check failed{Colors.ENDC}")
        print(f"   Error: {result.get('error', 'Unknown error')}")
    
    return result

async def test_nlp_status():
    """Test NLP Engine status from Speech Service"""
    print(f"\n{Colors.BLUE}Testing NLP Engine Status from Speech Service...{Colors.ENDC}")
    result = await test_endpoint(f"{SPEECH_SERVICE_URL}/api/nlp-status")
    
    if result["success"]:
        data = result["data"]
        if data.get("nlp_engine_status") == "healthy":
            print(f"{Colors.GREEN}✅ NLP Engine is reachable from Speech Service{Colors.ENDC}")
        else:
            print(f"{Colors.YELLOW}⚠️  NLP Engine status: {data.get('nlp_engine_status')}{Colors.ENDC}")
    else:
        print(f"{Colors.RED}❌ Failed to check NLP Engine status{Colors.ENDC}")
        print(f"   Error: {result.get('error', 'Unknown error')}")
    
    return result

async def test_voice_command_examples():
    """Test voice command examples from NLP Engine"""
    print(f"\n{Colors.BLUE}Testing Voice Command Examples...{Colors.ENDC}")
    result = await test_endpoint(f"{NLP_ENGINE_URL}/api/examples")
    
    if result["success"]:
        print(f"{Colors.GREEN}✅ Voice command examples retrieved{Colors.ENDC}")
        examples = result["data"]
        
        print(f"\n{Colors.YELLOW}Task Management Examples:{Colors.ENDC}")
        for i, example in enumerate(examples.get("task_management", [])[:3], 1):
            print(f"   {i}. {example}")
        
        print(f"\n{Colors.YELLOW}Natural Language Examples:{Colors.ENDC}")
        for i, example in enumerate(examples.get("natural_language", [])[:3], 1):
            print(f"   {i}. {example}")
    else:
        print(f"{Colors.RED}❌ Failed to get voice command examples{Colors.ENDC}")
    
    return result

async def test_nlp_processing():
    """Test NLP processing with sample commands"""
    print(f"\n{Colors.BLUE}Testing NLP Processing...{Colors.ENDC}")
    
    test_commands = [
        "Create a task called 'Buy groceries'",
        "Show my tasks",
        "What are my task statistics?",
        "Complete task 'Buy groceries'",
        "Hello world"
    ]
    
    for command in test_commands:
        print(f"\n{Colors.YELLOW}Testing: \"{command}\"{Colors.ENDC}")
        result = await test_endpoint(
            f"{NLP_ENGINE_URL}/api/process",
            method="POST",
            data={
                "text": command,
                "context": {},
                "execute": False
            }
        )
        
        if result["success"]:
            data = result["data"]
            intent = data.get("intent", "unknown")
            confidence = data.get("confidence", 0)
            response = data.get("response", "No response")
            
            print(f"   Intent: {intent}")
            print(f"   Confidence: {confidence:.2f}")
            print(f"   Response: {response}")
        else:
            print(f"   {Colors.RED}Failed to process command{Colors.ENDC}")

async def test_text_to_speech():
    """Test text-to-speech functionality"""
    print(f"\n{Colors.BLUE}Testing Text-to-Speech...{Colors.ENDC}")
    
    test_text = "Hello, this is a test of the text to speech functionality."
    result = await test_endpoint(
        f"{SPEECH_SERVICE_URL}/api/text-to-speech",
        method="POST",
        data={
            "text": test_text,
            "voice": "default",
            "rate": 150,
            "volume": 1.0
        }
    )
    
    if result["success"]:
        print(f"{Colors.GREEN}✅ Text-to-speech test completed{Colors.ENDC}")
        print(f"   Text: {test_text}")
    else:
        print(f"{Colors.RED}❌ Text-to-speech test failed{Colors.ENDC}")
        print(f"   Error: {result.get('error', 'Unknown error')}")

async def test_available_voices():
    """Test getting available voices"""
    print(f"\n{Colors.BLUE}Testing Available Voices...{Colors.ENDC}")
    result = await test_endpoint(f"{SPEECH_SERVICE_URL}/api/voices")
    
    if result["success"]:
        voices = result["data"].get("voices", [])
        print(f"{Colors.GREEN}✅ Found {len(voices)} available voices{Colors.ENDC}")
        
        for i, voice in enumerate(voices[:3], 1):
            print(f"   {i}. {voice.get('name', 'Unknown')} ({voice.get('id', 'N/A')})")
    else:
        print(f"{Colors.RED}❌ Failed to get available voices{Colors.ENDC}")

async def main():
    """Run all integration tests"""
    print(f"{Colors.BLUE}🧠 SmartJARVIS Integration Test{Colors.ENDC}")
    print(f"{Colors.BLUE}Speech Service URL: {SPEECH_SERVICE_URL}{Colors.ENDC}")
    print(f"{Colors.BLUE}NLP Engine URL: {NLP_ENGINE_URL}{Colors.ENDC}")
    print("=" * 60)
    
    # Run tests
    await test_speech_service_health()
    await test_nlp_engine_health()
    await test_nlp_status()
    await test_voice_command_examples()
    await test_nlp_processing()
    await test_text_to_speech()
    await test_available_voices()
    
    print(f"\n{Colors.GREEN}🎉 Integration testing completed!{Colors.ENDC}")
    print(f"{Colors.YELLOW}Note: Some tests may fail if services are not running{Colors.ENDC}")

if __name__ == "__main__":
    asyncio.run(main()) 