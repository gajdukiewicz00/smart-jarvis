# E2E WebSocket Audio Tester Tests

End-to-end tests for SmartJARVIS WebSocket Audio Tester with comprehensive quality analysis and performance benchmarks.

## 🎯 Test Objectives

- **Round Trip Time (RTT)**: Verify RTT < 300ms for voice roundtrip
- **Audio Quality**: Analyze signal-to-noise ratio, distortion, and dynamic range
- **Connection Stability**: Test multiple concurrent connections
- **Performance**: Benchmark latency and throughput
- **Sine Wave Test**: Generate and analyze 2-second 440Hz sine wave

## 🧪 Test Components

### Node.js Tests (`ws-audio-tester.test.js`)
- WebSocket connection testing
- Audio generation and transmission
- Round trip time measurement
- Connection stability testing
- Basic audio quality validation

### Python Tests (`ws_audio_analyzer.py`)
- Advanced audio quality analysis
- Signal-to-noise ratio calculation
- Total harmonic distortion measurement
- Frequency response analysis
- Dynamic range assessment
- Comprehensive performance metrics

## 🚀 Running Tests

### Prerequisites
```bash
# Install Node.js dependencies
npm install

# Install Python dependencies
pip install -r requirements.txt

# Start SmartJARVIS services
cd ../../docker
docker-compose up -d
```

### Manual Testing
```bash
# Run Node.js tests
npm test

# Run Python tests
npm run test:python

# Run all tests
npm run test:all

# Run benchmarks
npm run benchmark
```

### CI/CD Testing
```bash
# Trigger E2E tests via label
gh pr edit --add-label "run-e2e"

# Or run manually
gh workflow run e2e-ws-audio.yml
```

## 📊 Test Metrics

### Performance Requirements
- **RTT**: ≤ 300ms
- **Latency**: ≤ 100ms
- **Audio Quality Score**: ≥ 70/100
- **SNR**: ≥ 20dB
- **Distortion**: ≤ 0.1 (10%)
- **Stability**: ≥ 80% success rate

### Test Configuration
```javascript
const CONFIG = {
    host: 'localhost',
    port: 7090,
    protocol: 'ws',
    maxRTT: 300,        // ms
    testDuration: 2000, // ms
    sampleRate: 16000,  // Hz
    frequency: 440      // Hz
};
```

## 📈 Test Reports

### Node.js Report (`e2e-test-report.json`)
```json
{
  "summary": {
    "totalTests": 5,
    "passedTests": 4,
    "successRate": 80,
    "averageRTT": 250.5
  },
  "results": {
    "connection": true,
    "audioGeneration": true,
    "roundTripTime": 245,
    "audioQuality": true,
    "stability": true
  }
}
```

### Python Report (`e2e-audio-report.json`)
```json
{
  "summary": {
    "success_rate": 85.0,
    "average_rtt": 245.2,
    "average_quality": 78.5
  },
  "results": [{
    "rtt_ms": 245.2,
    "audio_quality_score": 78.5,
    "signal_to_noise_ratio": 25.3,
    "distortion_level": 0.08,
    "dynamic_range": 45.2,
    "test_passed": true
  }]
}
```

## 🔧 Test Environment

### Service Dependencies
- **Gateway**: `http://localhost:8080`
- **Task Service**: `http://localhost:8081`
- **NLP Engine**: `http://localhost:3001`
- **Speech Service**: `http://localhost:8083`
- **WebSocket**: `ws://localhost:7090/ws`

### Environment Variables
```bash
export WS_HOST=localhost
export WS_PORT=7090
export WS_PROTOCOL=ws
export MAX_RTT_MS=300
export TEST_DURATION_MS=2000
```

## 🐛 Troubleshooting

### Common Issues

**Connection Failed**
```bash
# Check if services are running
curl http://localhost:8080/actuator/health

# Check WebSocket endpoint
wscat -c ws://localhost:7090/ws
```

**High RTT**
```bash
# Check network latency
ping localhost

# Check service health
docker-compose ps
```

**Audio Quality Issues**
```bash
# Check audio service logs
docker-compose logs speech-service

# Verify audio configuration
curl http://localhost:8083/health
```

### Debug Mode
```bash
# Enable debug logging
export DEBUG=true
npm test

# Verbose Python output
python ws_audio_analyzer.py --verbose
```

## 📋 Test Checklist

- [ ] All services running and healthy
- [ ] WebSocket connection established
- [ ] Audio generation working
- [ ] Round trip time < 300ms
- [ ] Audio quality score ≥ 70
- [ ] SNR ≥ 20dB
- [ ] Distortion ≤ 10%
- [ ] Stability test passes
- [ ] Performance benchmarks pass

## 🔄 Continuous Integration

### GitHub Actions
- **Trigger**: Label `run-e2e` on PR/Issue
- **Manual**: Workflow dispatch
- **Environment**: Ubuntu latest with Docker
- **Artifacts**: Test reports and logs

### Branch Protection
- E2E tests required for `main` branch
- Performance regression detection
- Quality gate enforcement

## 📚 Additional Resources

- [WebSocket Audio Tester](../clients/js/ws-audio-tester/README.md)
- [SmartJARVIS Architecture](../../README.md)
- [Docker Setup](../../docker/README.md)
- [CI/CD Pipeline](../../.github/workflows/)
