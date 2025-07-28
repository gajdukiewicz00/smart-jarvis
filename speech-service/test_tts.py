#!/usr/bin/env python3
"""
Test script for Speech Service TTS functionality
"""

import requests
import json
import tempfile
import os

# Configuration
SPEECH_SERVICE_URL = "http://localhost:8083"

def test_tts_endpoint():
    """Test the text-to-speech endpoint"""
    print("🎤 Testing TTS endpoint...")
    
    # Test data
    test_text = "Hello, this is a test of the text to speech functionality."
    
    try:
        # Make request to TTS endpoint
        response = requests.post(
            f"{SPEECH_SERVICE_URL}/api/text-to-speech",
            json={
                "text": test_text,
                "voice": "en"
            },
            timeout=30
        )
        
        if response.status_code == 200:
            # Save audio file
            with tempfile.NamedTemporaryFile(delete=False, suffix=".mp3") as temp_file:
                temp_file.write(response.content)
                temp_file_path = temp_file.name
            
            print(f"✅ TTS successful! Audio saved to: {temp_file_path}")
            print(f"📊 Response headers: {dict(response.headers)}")
            return True
        else:
            print(f"❌ TTS failed with status {response.status_code}")
            print(f"Response: {response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Error testing TTS: {e}")
        return False

def test_voices_endpoint():
    """Test the voices endpoint"""
    print("\n🎵 Testing voices endpoint...")
    
    try:
        response = requests.get(f"{SPEECH_SERVICE_URL}/api/voices")
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Voices endpoint successful!")
            print(f"Available voices: {data.get('voices', {})}")
            return True
        else:
            print(f"❌ Voices endpoint failed with status {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Error testing voices: {e}")
        return False

def test_health_endpoint():
    """Test the health endpoint"""
    print("\n🏥 Testing health endpoint...")
    
    try:
        response = requests.get(f"{SPEECH_SERVICE_URL}/health")
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ Health check successful!")
            print(f"Status: {data.get('status')}")
            print(f"Service: {data.get('service')}")
            return True
        else:
            print(f"❌ Health check failed with status {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Error testing health: {e}")
        return False

def test_nlp_status():
    """Test NLP Engine status"""
    print("\n🧠 Testing NLP Engine status...")
    
    try:
        response = requests.get(f"{SPEECH_SERVICE_URL}/api/nlp-status")
        
        if response.status_code == 200:
            data = response.json()
            print(f"✅ NLP status check successful!")
            print(f"NLP Engine status: {data.get('nlp_engine_status')}")
            print(f"NLP Engine URL: {data.get('nlp_engine_url')}")
            return True
        else:
            print(f"❌ NLP status check failed with status {response.status_code}")
            return False
            
    except Exception as e:
        print(f"❌ Error testing NLP status: {e}")
        return False

def main():
    """Run all tests"""
    print("🚀 Starting Speech Service TTS tests...")
    print(f"Target URL: {SPEECH_SERVICE_URL}")
    
    # Test health first
    health_ok = test_health_endpoint()
    
    if not health_ok:
        print("❌ Speech Service is not responding. Make sure it's running.")
        return
    
    # Test other endpoints
    voices_ok = test_voices_endpoint()
    tts_ok = test_tts_endpoint()
    nlp_ok = test_nlp_status()
    
    # Summary
    print("\n📊 Test Summary:")
    print(f"Health Check: {'✅' if health_ok else '❌'}")
    print(f"Voices Endpoint: {'✅' if voices_ok else '❌'}")
    print(f"TTS Endpoint: {'✅' if tts_ok else '❌'}")
    print(f"NLP Status: {'✅' if nlp_ok else '❌'}")
    
    if all([health_ok, voices_ok, tts_ok, nlp_ok]):
        print("\n🎉 All tests passed! TTS functionality is working.")
    else:
        print("\n⚠️ Some tests failed. Check the logs above.")

if __name__ == "__main__":
    main() 