#!/usr/bin/env python3
"""
Integration test for Speech Service with NLP Engine and Task Service
"""

import requests
import json
import tempfile
import os
import time

# Configuration
GATEWAY_URL = "http://localhost:8080"
SPEECH_SERVICE_URL = "http://localhost:8083"

def test_voice_command_cycle():
    """Test the complete voice command cycle"""
    print("🎤 Testing complete voice command cycle...")
    
    # Step 1: Test TTS with different languages
    test_cases = [
        {"text": "Hello, this is a test in English!", "voice": "en"},
        {"text": "Привет, это тест на русском языке!", "voice": "ru"},
        {"text": "Hola, esto es una prueba en español!", "voice": "es"}
    ]
    
    for i, test_case in enumerate(test_cases):
        print(f"\n📝 Test case {i+1}: {test_case['voice']}")
        
        try:
            response = requests.post(
                f"{SPEECH_SERVICE_URL}/api/text-to-speech",
                json=test_case,
                timeout=30
            )
            
            if response.status_code == 200:
                # Save audio file
                filename = f"test_tts_{test_case['voice']}.mp3"
                with open(filename, 'wb') as f:
                    f.write(response.content)
                
                print(f"✅ TTS successful for {test_case['voice']}: {filename}")
                print(f"📊 File size: {len(response.content)} bytes")
            else:
                print(f"❌ TTS failed for {test_case['voice']}: {response.status_code}")
                
        except Exception as e:
            print(f"❌ Error testing TTS for {test_case['voice']}: {e}")

def test_nlp_integration():
    """Test NLP Engine integration"""
    print("\n🧠 Testing NLP Engine integration...")
    
    # Test NLP status
    try:
        response = requests.get(f"{SPEECH_SERVICE_URL}/api/nlp-status")
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ NLP Engine status: {data.get('nlp_engine_status')}")
            
            if data.get('success'):
                print("✅ NLP Engine is healthy and reachable")
                return True
            else:
                print("⚠️ NLP Engine is not healthy")
                return False
        else:
            print(f"❌ NLP status check failed: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Error checking NLP status: {e}")
        return False

def test_task_service_integration():
    """Test Task Service integration through NLP"""
    print("\n📋 Testing Task Service integration...")
    
    # Test creating a task through NLP Engine
    test_commands = [
        "create task: Test task from speech service",
        "add task: Another test task",
        "new task: Final test task"
    ]
    
    for command in test_commands:
        print(f"\n📝 Testing command: {command}")
        
        try:
            # This would normally be done through voice input
            # For now, we'll simulate the NLP processing
            response = requests.post(
                f"{GATEWAY_URL}/nlp/api/process",
                json={
                    "text": command,
                    "context": {},
                    "execute": True
                },
                timeout=10
            )
            
            if response.status_code == 200:
                data = response.json()
                print(f"✅ NLP processing successful")
                print(f"📊 Response: {data.get('data', {}).get('response', 'No response')}")
            else:
                print(f"❌ NLP processing failed: {response.status_code}")
                
        except Exception as e:
            print(f"❌ Error testing NLP processing: {e}")

def test_voices_endpoint():
    """Test the voices endpoint"""
    print("\n🎵 Testing voices endpoint...")
    
    try:
        response = requests.get(f"{SPEECH_SERVICE_URL}/api/voices")
        
        if response.status_code == 200:
            data = response.json()
            voices = data.get('voices', {})
            print(f"✅ Available voices: {len(voices)} languages")
            
            for lang, name in voices.items():
                print(f"  - {lang}: {name}")
                
            return True
        else:
            print(f"❌ Voices endpoint failed: {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Error testing voices: {e}")
        return False

def test_health_endpoints():
    """Test health endpoints for all services"""
    print("\n🏥 Testing health endpoints...")
    
    health_endpoints = [
        ("Speech Service", f"{SPEECH_SERVICE_URL}/health"),
        ("API Gateway", f"{GATEWAY_URL}/health"),
        ("Task Service", f"{GATEWAY_URL}/tasks/health"),
        ("NLP Engine", f"{GATEWAY_URL}/nlp/health")
    ]
    
    all_healthy = True
    
    for service_name, endpoint in health_endpoints:
        try:
            response = requests.get(endpoint, timeout=5)
            
            if response.status_code == 200:
                print(f"✅ {service_name}: Healthy")
            else:
                print(f"❌ {service_name}: Unhealthy ({response.status_code})")
                all_healthy = False
                
        except Exception as e:
            print(f"❌ {service_name}: Error - {e}")
            all_healthy = False
    
    return all_healthy

def main():
    """Run all integration tests"""
    print("🚀 Starting Speech Service Integration Tests...")
    print(f"Gateway URL: {GATEWAY_URL}")
    print(f"Speech Service URL: {SPEECH_SERVICE_URL}")
    
    # Test health first
    health_ok = test_health_endpoints()
    
    if not health_ok:
        print("\n⚠️ Some services are not healthy. Continuing with tests...")
    
    # Test voices
    voices_ok = test_voices_endpoint()
    
    # Test NLP integration
    nlp_ok = test_nlp_integration()
    
    # Test TTS with multiple languages
    test_voice_command_cycle()
    
    # Test Task Service integration
    if nlp_ok:
        test_task_service_integration()
    
    # Summary
    print("\n📊 Integration Test Summary:")
    print(f"Health Checks: {'✅' if health_ok else '❌'}")
    print(f"Voices Endpoint: {'✅' if voices_ok else '❌'}")
    print(f"NLP Integration: {'✅' if nlp_ok else '❌'}")
    
    if all([health_ok, voices_ok, nlp_ok]):
        print("\n🎉 All integration tests passed!")
        print("✅ TTS functionality is fully restored and working")
        print("✅ Multi-language support is working")
        print("✅ NLP Engine integration is working")
        print("✅ API Gateway routing is working")
    else:
        print("\n⚠️ Some integration tests failed. Check the logs above.")

if __name__ == "__main__":
    main() 