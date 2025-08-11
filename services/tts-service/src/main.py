from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import uvicorn
import os
import time
import json
import base64
from typing import Dict, Any
from kafka import KafkaProducer
import avro.schema
from avro.io import DatumWriter, BinaryEncoder
import io
import subprocess
import tempfile
import hashlib

app = FastAPI(title="TTS Service", version="1.0.0")

# Kafka setup
producer = KafkaProducer(
    bootstrap_servers=os.getenv('KAFKA_BROKERS', 'kafka:9092').split(','),
    value_serializer=lambda v: json.dumps(v).encode('utf-8') # Placeholder, will use Avro
)

# TTS backend selection
tts_backend = os.getenv('TTS_BACKEND', 'piper')
tts_voice = os.getenv('TTS_VOICE', 'en_US-amy-medium')

# Simple TTS cache for short phrases
tts_cache = {}

def get_cache_key(text: str, voice: str) -> str:
    """Generate cache key for text + voice combination"""
    return hashlib.md5(f"{text}:{voice}".encode()).hexdigest()

def synthesize_speech_piper(text: str, voice: str = None) -> bytes:
    """Synthesize speech using Piper TTS"""
    if not voice:
        voice = tts_voice
    
    try:
        # Check if we have the voice model
        voice_path = f"/voices/{voice}.onnx"
        if not os.path.exists(voice_path):
            print(f"Voice model not found: {voice_path}")
            # Fallback to dummy audio
            return generate_dummy_audio(text)
        
        # Use Piper to synthesize
        with tempfile.NamedTemporaryFile(suffix=".wav") as f:
            cmd = ["piper", "-m", voice_path, "-t", text, "-o", f.name]
            result = subprocess.run(cmd, capture_output=True, text=True, check=True)
            
            if result.returncode == 0:
                f.seek(0)
                audio_data = f.read()
                print(f"Piper TTS successful: {text[:50]}...")
                return audio_data
            else:
                print(f"Piper TTS failed: {result.stderr}")
                return generate_dummy_audio(text)
                
    except subprocess.CalledProcessError as e:
        print(f"Piper TTS error: {e}")
        return generate_dummy_audio(text)
    except Exception as e:
        print(f"Piper TTS exception: {e}")
        return generate_dummy_audio(text)

def generate_dummy_audio(text: str) -> bytes:
    """Generate dummy WAV audio for fallback"""
    # Minimal WAV header + silence
    sample_rate = 16000
    duration_ms = max(1000, len(text) * 100)  # 1 second minimum, 100ms per character
    samples = int(sample_rate * duration_ms / 1000)
    
    # WAV header
    wav_header = (
        b"RIFF" +
        (samples * 2 + 36).to_bytes(4, 'little') +  # File size
        b"WAVE" +
        b"fmt " +
        (16).to_bytes(4, 'little') +  # Chunk size
        (1).to_bytes(2, 'little') +   # Audio format (PCM)
        (1).to_bytes(2, 'little') +   # Channels
        sample_rate.to_bytes(4, 'little') +  # Sample rate
        (sample_rate * 2).to_bytes(4, 'little') +  # Byte rate
        (2).to_bytes(2, 'little') +   # Block align
        (16).to_bytes(2, 'little') +  # Bits per sample
        b"data" +
        (samples * 2).to_bytes(4, 'little')  # Data size
    )
    
    # Generate silence (16-bit PCM)
    silence = b"\x00\x00" * samples
    
    return wav_header + silence

def synthesize_speech(text: str, voice: str = None) -> bytes:
    """Main TTS function with caching"""
    if not voice:
        voice = tts_voice
    
    # Check cache first
    cache_key = get_cache_key(text, voice)
    if cache_key in tts_cache:
        print(f"Using cached audio for: {text[:50]}...")
        return tts_cache[cache_key]
    
    # Synthesize new audio
    if tts_backend == 'piper':
        audio_data = synthesize_speech_piper(text, voice)
    else:
        # Fallback to dummy audio
        audio_data = generate_dummy_audio(text)
    
    # Cache the result (limit cache size)
    if len(tts_cache) < 100:  # Max 100 cached phrases
        tts_cache[cache_key] = audio_data
        print(f"Cached audio for: {text[:50]}...")
    
    return audio_data

@app.get("/healthz")
async def health_check():
    return JSONResponse(content={"status": "ok"})

@app.get("/readyz")
async def ready_check():
    kafka_ready = False
    try:
        producer.bootstrap_connected() # Check Kafka connection
        kafka_ready = True
    except Exception:
        pass
    
    # Check TTS backend
    tts_ready = False
    if tts_backend == 'piper':
        voice_path = f"/voices/{tts_voice}.onnx"
        tts_ready = os.path.exists(voice_path)
    else:
        tts_ready = True  # Dummy backend is always ready
    
    return JSONResponse(content={
        "status": "ok" if kafka_ready and tts_ready else "not ready", 
        "kafka_ready": kafka_ready,
        "tts_ready": tts_ready,
        "backend": tts_backend,
        "voice": tts_voice
    }, status_code=200 if kafka_ready and tts_ready else 503)

@app.post("/synthesize")
async def synthesize_endpoint(request: Dict[str, Any]):
    """Synthesize speech (for testing)"""
    try:
        text = request.get("text", "")
        voice = request.get("voice", tts_voice)
        
        if not text:
            raise HTTPException(status_code=400, detail="Missing text field")
        
        start_time = time.time()
        audio_data = synthesize_speech(text, voice)
        latency_ms = (time.time() - start_time) * 1000
        
        return JSONResponse(content={
            "status": "synthesized",
            "text": text,
            "voice": voice,
            "audioSize": len(audio_data),
            "latencyMs": latency_ms,
            "audioData": base64.b64encode(audio_data).decode('utf-8')
        })
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

async def process_tts_request(message: any):
    """Process TTS request from Kafka"""
    start_time = time.time()
    
    try:
        text = message.get("text", "")
        ssml = message.get("ssml", "")
        voice = message.get("voice", tts_voice)
        session_id = message.get("sessionId")
        correlation_id = message.get("correlationId")
        
        if not text or not session_id or not correlation_id:
            print(f"Missing required fields in TTS request: {message}")
            return
        
        # Use SSML if available, otherwise plain text
        synthesis_text = ssml if ssml else text
        
        # Synthesize speech
        audio_data = synthesize_speech(synthesis_text, voice)
        
        # Publish to Kafka
        tts_message = {
            "messageVersion": 1,
            "timestamp": int(time.time() * 1000),
            "correlationId": correlation_id,
            "sessionId": session_id,
            "audioFormat": "WAV",
            "sampleRate": 16000,
            "audioData": base64.b64encode(audio_data).decode('utf-8'),
            "originalText": text,
            "ssml": ssml,
            "voice": voice,
            "errors": []
        }
        
        await producer.send('sj.tts.audio.v1', session_id, tts_message)
        
        # Record latency
        latency_ms = (time.time() - start_time) * 1000
        print(f"TTS completed in {latency_ms:.1f}ms: {text[:50]}...")
        
    except Exception as e:
        print(f"Error processing TTS request: {e}")
        
        # Send to DLQ
        try:
            dlq_message = {
                "originalTopic": "sj.tts.request.v1",
                "error": str(e),
                "timestamp": int(time.time() * 1000),
                "payload": message
            }
            await producer.send('sj.tts.request.dlq.v1', message.get('sessionId', 'unknown'), dlq_message)
        except Exception as dlq_error:
            print(f"Failed to send to DLQ: {dlq_error}")

# Kafka consumer for TTS requests
def setup_kafka_consumer():
    """Setup Kafka consumer for TTS processing"""
    from kafka import KafkaConsumer
    
    consumer = KafkaConsumer(
        'sj.tts.request.v1',
        bootstrap_servers=os.getenv('KAFKA_BROKERS', 'kafka:9092').split(','),
        group_id='tts-service-group',
        auto_offset_reset='earliest',
        enable_auto_commit=True
    )
    
    print("TTS Service listening to sj.tts.request.v1")
    
    for message in consumer:
        try:
            value = json.loads(message.value.decode('utf-8'))
            asyncio.run(process_tts_request(value)) # Run async function in a thread
        except Exception as e:
            print(f"Error processing TTS request: {e}")
            # Send to DLQ
            try:
                error_message = {
                    "originalTopic": "sj.tts.request.v1",
                    "error": str(e),
                    "timestamp": int(time.time() * 1000),
                    "payload": message.value.decode('utf-8') if message.value else ""
                }
                producer.send('sj.tts.request.dlq.v1', 'unknown', error_message)
            except:
                pass

if __name__ == "__main__":
    import threading
    import asyncio
    
    # Start Kafka consumer in background thread
    consumer_thread = threading.Thread(target=setup_kafka_consumer, daemon=True)
    consumer_thread.start()
    
    # Start FastAPI server
    uvicorn.run(app, host="0.0.0.0", port=7094)
