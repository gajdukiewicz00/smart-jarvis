### Voice Demo (Increment A)

1) Setup env
```
cd platform/docker
cp env.sample .env
```

2) Start stack
```
make -C ../.. up-voice
```

3) Test health
```
curl -f http://localhost:7090/healthz
```

4) Test via WebSocket
- Connect to `ws://localhost:7090/ws/audio`
- First binary frame: JSON header `{sessionId, correlationId, sampleRate}`
- Subsequent frames: raw PCM16 mono 16kHz with frame size 20–40ms
- Voice gateway streams back `tts.audio` as PCM/WAV with matching `sessionId`

5) Observe metrics/traces
- Grafana at http://localhost:3000
- Prometheus at http://localhost:9090

Notes:
- Only voice-gateway publishes port 7090
- Legacy services remain available on 8080–8083
