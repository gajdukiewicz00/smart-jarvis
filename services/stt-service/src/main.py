from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import uvicorn
import os
import time
import json
import base64
from typing import Dict, Any
from vosk import Model, KaldiRecognizer
import numpy as np
from kafka import KafkaProducer
import avro.schema
from avro.io import DatumWriter, BinaryEncoder
import io

app = FastAPI(title="STT Service", version="1.0.0")

# Kafka setup
producer = KafkaProducer(
    bootstrap_servers=os.getenv('KAFKA_BROKERS', 'kafka:9092').split(','),
    value_serializer=lambda v: json.dumps(v).encode('utf-8') # Placeholder, will use Avro
)

# Vosk model
model_path = os.getenv('STT_MODEL', 'vosk-model-small-en-us-0.15')
model = None
recognizer = None
model_loaded = False

# STT processing state
last_partial_emit = 0.0
PARTIAL_INTERVAL = 0.3  # 300ms

@app.on_event("startup")
async def startup_event():
    global model, recognizer, model_loaded
    try:
        model = Model(model_path)
        recognizer = KaldiRecognizer(model, 16000)
        model_loaded = True
        print(f"Vosk model loaded from {model_path}")
    except Exception as e:
        print(f"Failed to load Vosk model: {e}")
        model_loaded = False

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
    return JSONResponse(content={"status": "ok" if model_loaded and kafka_ready else "not ready", "model_loaded": model_loaded, "kafka_ready": kafka_ready},
                        status_code=200 if model_loaded and kafka_ready else 503)

def publish_partial(text: str, session_id: str, correlation_id: str, start_time: int):
    """Publish partial STT result"""
    if not text or text.strip() == "":
        return
        
    message = {
        "messageVersion": 1,
        "timestamp": int(time.time() * 1000),
        "correlationId": correlation_id,
        "sessionId": session_id,
        "text": text,
        "startMs": start_time,
        "endMs": int(time.time() * 1000),
        "confidence": 0.5,  # Vosk doesn't provide confidence for partial
        "lang": "en",
        "isFinal": False,
        "errors": []
    }
    
    try:
        producer.send('sj.stt.partial.v1', session_id, message)
        print(f"Partial STT: {text[:50]}...")
    except Exception as e:
        print(f"Failed to publish partial STT: {e}")
        # Send to DLQ
        dlq_message = {
            "originalTopic": "sj.stt.partial.v1",
            "error": str(e),
            "timestamp": int(time.time() * 1000),
            "payload": message
        }
        producer.send('sj.stt.partial.dlq.v1', session_id, dlq_message)

def publish_final(text: str, session_id: str, correlation_id: str, start_time: int, confidence: float = 0.8):
    """Publish final STT result"""
    if not text or text.strip() == "":
        return
        
    message = {
        "messageVersion": 1,
        "timestamp": int(time.time() * 1000),
        "correlationId": correlation_id,
        "sessionId": session_id,
        "text": text,
        "startMs": start_time,
        "endMs": int(time.time() * 1000),
        "confidence": confidence,
        "lang": "en",
        "isFinal": True,
        "errors": []
    }
    
    try:
        producer.send('sj.stt.final.v1', session_id, message)
        print(f"Final STT: {text}")
    except Exception as e:
        print(f"Failed to publish final STT: {e}")
        # Send to DLQ
        dlq_message = {
            "originalTopic": "sj.stt.final.v1",
            "error": str(e),
            "timestamp": int(time.time() * 1000),
            "payload": message
        }
        producer.send('sj.stt.final.dlq.v1', session_id, dlq_message)

def process_audio_chunk(audio_data: bytes, session_id: str, correlation_id: str, start_time: int):
    """Process audio chunk with Vosk"""
    global last_partial_emit
    
    try:
        # Feed audio to Vosk
        if recognizer.AcceptWaveform(audio_data):
            # Final result available
            result = json.loads(recognizer.Result())
            text = result.get("text", "")
            if text:
                publish_final(text, session_id, correlation_id, start_time)
        else:
            # Check if we should emit partial result
            current_time = time.time()
            if current_time - last_partial_emit > PARTIAL_INTERVAL:
                partial_result = json.loads(recognizer.PartialResult())
                partial_text = partial_result.get("partial", "")
                if partial_text:
                    publish_partial(partial_text, session_id, correlation_id, start_time)
                    last_partial_emit = current_time
                    
    except Exception as e:
        print(f"Error processing audio chunk: {e}")
        # Send error to DLQ
        error_message = {
            "originalTopic": "sj.audio.raw.v1",
            "error": str(e),
            "timestamp": int(time.time() * 1000),
            "sessionId": session_id,
            "correlationId": correlation_id
        }
        producer.send('sj.audio.raw.dlq.v1', session_id, error_message)

@app.post("/process_audio")
async def process_audio_endpoint(request: Dict[str, Any]):
    """Process audio data (for testing)"""
    try:
        audio_data = base64.b64decode(request.get("audioData", ""))
        session_id = request.get("sessionId")
        correlation_id = request.get("correlationId")
        start_time = request.get("startTime", int(time.time() * 1000))
        
        if not all([audio_data, session_id, correlation_id]):
            raise HTTPException(status_code=400, detail="Missing required fields")
            
        process_audio_chunk(audio_data, session_id, correlation_id, start_time)
        return JSONResponse(content={"status": "processed"})
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# Kafka consumer for audio.raw.v1
def setup_kafka_consumer():
    """Setup Kafka consumer for audio processing"""
    from kafka import KafkaConsumer
    
    consumer = KafkaConsumer(
        'sj.audio.raw.v1',
        bootstrap_servers=os.getenv('KAFKA_BROKERS', 'kafka:9092').split(','),
        group_id='stt-service-group',
        auto_offset_reset='earliest',
        enable_auto_commit=True
    )
    
    print("STT Service listening to sj.audio.raw.v1")
    
    for message in consumer:
        try:
            value = json.loads(message.value.decode('utf-8'))
            audio_data = base64.b64decode(value.get("audioData", ""))
            session_id = value.get("sessionId")
            correlation_id = value.get("correlationId")
            timestamp = value.get("timestamp", int(time.time() * 1000))
            
            if audio_data and session_id and correlation_id:
                process_audio_chunk(audio_data, session_id, correlation_id, timestamp)
                
        except Exception as e:
            print(f"Error processing Kafka message: {e}")
            # Send to DLQ
            try:
                error_message = {
                    "originalTopic": "sj.audio.raw.v1",
                    "error": str(e),
                    "timestamp": int(time.time() * 1000),
                    "payload": message.value.decode('utf-8') if message.value else ""
                }
                producer.send('sj.audio.raw.dlq.v1', 'unknown', error_message)
            except:
                pass

if __name__ == "__main__":
    import threading
    
    # Start Kafka consumer in background thread
    consumer_thread = threading.Thread(target=setup_kafka_consumer, daemon=True)
    consumer_thread.start()
    
    # Start FastAPI server
    uvicorn.run(app, host="0.0.0.0", port=7091)
