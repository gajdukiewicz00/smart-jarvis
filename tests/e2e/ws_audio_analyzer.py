#!/usr/bin/env python3
"""
E2E Audio Quality Analyzer for WebSocket Audio Tester
Analyzes audio quality, latency, and performance metrics
"""

import asyncio
import websockets
import json
import numpy as np
import time
import struct
import os
import sys
from typing import List, Dict, Any, Optional
from dataclasses import dataclass
from pathlib import Path

@dataclass
class AudioMetrics:
    """Audio quality metrics"""
    rtt_ms: float
    latency_ms: float
    audio_quality_score: float
    signal_to_noise_ratio: float
    frequency_response: Dict[str, float]
    distortion_level: float
    dynamic_range: float
    test_passed: bool

@dataclass
class TestConfig:
    """Test configuration"""
    host: str = "localhost"
    port: int = 7090
    protocol: str = "ws"
    max_rtt_ms: float = 300.0
    test_duration_ms: int = 2000
    sample_rate: int = 16000
    chunk_size: int = 1024
    frequency: float = 440.0  # Hz
    amplitude: float = 0.3

class WebSocketAudioAnalyzer:
    """Advanced WebSocket Audio Tester with quality analysis"""
    
    def __init__(self, config: TestConfig):
        self.config = config
        self.results: List[AudioMetrics] = []
        self.errors: List[str] = []
        
    def generate_sine_wave(self, duration_ms: int, frequency: float, sample_rate: int) -> np.ndarray:
        """Generate sine wave audio data"""
        samples = int(duration_ms * sample_rate / 1000)
        t = np.linspace(0, duration_ms / 1000, samples, False)
        wave = self.config.amplitude * np.sin(2 * np.pi * frequency * t)
        return (wave * 32767).astype(np.int16)
    
    def analyze_audio_quality(self, audio_data: np.ndarray) -> Dict[str, float]:
        """Analyze audio quality metrics"""
        if len(audio_data) == 0:
            return {"quality_score": 0.0, "snr": 0.0, "distortion": 1.0}
        
        # Signal-to-Noise Ratio
        signal_power = np.mean(audio_data ** 2)
        noise_floor = np.var(audio_data - np.mean(audio_data))
        snr = 10 * np.log10(signal_power / (noise_floor + 1e-10))
        
        # Total Harmonic Distortion (simplified)
        fft = np.fft.fft(audio_data)
        fft_magnitude = np.abs(fft)
        fundamental = fft_magnitude[0]
        harmonics = fft_magnitude[1:len(fft_magnitude)//2]
        thd = np.sqrt(np.sum(harmonics ** 2)) / (fundamental + 1e-10)
        
        # Dynamic Range
        dynamic_range = 20 * np.log10(np.max(np.abs(audio_data)) / (np.min(np.abs(audio_data[np.abs(audio_data) > 0])) + 1e-10))
        
        # Overall Quality Score (0-100)
        quality_score = min(100, max(0, 
            (snr / 60) * 40 +  # SNR component (40 points)
            (1 - min(thd, 1)) * 30 +  # Distortion component (30 points)
            (dynamic_range / 60) * 30  # Dynamic range component (30 points)
        ))
        
        return {
            "quality_score": quality_score,
            "snr": snr,
            "distortion": thd,
            "dynamic_range": dynamic_range
        }
    
    def analyze_frequency_response(self, audio_data: np.ndarray) -> Dict[str, float]:
        """Analyze frequency response"""
        if len(audio_data) == 0:
            return {}
        
        fft = np.fft.fft(audio_data)
        freqs = np.fft.fftfreq(len(audio_data), 1/self.config.sample_rate)
        magnitude = np.abs(fft)
        
        # Find peak frequencies
        peak_indices = np.argsort(magnitude)[-5:]  # Top 5 peaks
        peak_freqs = freqs[peak_indices]
        peak_mags = magnitude[peak_indices]
        
        response = {}
        for freq, mag in zip(peak_freqs, peak_mags):
            if freq > 0:  # Only positive frequencies
                response[f"{freq:.1f}Hz"] = float(mag)
        
        return response
    
    async def test_connection(self) -> bool:
        """Test WebSocket connection"""
        try:
            uri = f"{self.config.protocol}://{self.config.host}:{self.config.port}/ws"
            print(f"🔌 Testing connection to {uri}")
            
            async with websockets.connect(uri, timeout=5) as websocket:
                print("✅ WebSocket connection successful")
                return True
                
        except Exception as e:
            print(f"❌ Connection failed: {e}")
            self.errors.append(f"Connection failed: {e}")
            return False
    
    async def test_audio_roundtrip(self) -> Optional[AudioMetrics]:
        """Test complete audio roundtrip with quality analysis"""
        try:
            uri = f"{self.config.protocol}://{self.config.host}:{self.config.port}/ws"
            print(f"🎵 Testing audio roundtrip...")
            
            async with websockets.connect(uri) as websocket:
                # Generate test audio
                audio_data = self.generate_sine_wave(
                    self.config.test_duration_ms,
                    self.config.frequency,
                    self.config.sample_rate
                )
                
                # Send session header
                header = {
                    "sessionId": f"test-{int(time.time())}",
                    "correlationId": f"corr-{int(time.time())}",
                    "sampleRate": self.config.sample_rate,
                    "protocolVersion": 1,
                    "testMode": True,
                    "expectedDuration": self.config.test_duration_ms
                }
                
                await websocket.send(json.dumps(header))
                
                # Record start time
                start_time = time.time()
                
                # Send audio data in chunks
                chunk_size = self.config.chunk_size
                for i in range(0, len(audio_data), chunk_size):
                    chunk = audio_data[i:i + chunk_size]
                    chunk_bytes = chunk.tobytes()
                    await websocket.send(chunk_bytes)
                    await asyncio.sleep(0.01)  # 10ms intervals
                
                # Send end marker
                end_marker = {"type": "end", "timestamp": time.time()}
                await websocket.send(json.dumps(end_marker))
                
                # Collect response
                response_data = []
                response_start = time.time()
                
                try:
                    async for message in websocket:
                        if isinstance(message, bytes):
                            # Convert bytes back to audio data
                            audio_chunk = np.frombuffer(message, dtype=np.int16)
                            response_data.extend(audio_chunk)
                        elif isinstance(message, str):
                            try:
                                data = json.loads(message)
                                if data.get("type") == "complete":
                                    break
                            except json.JSONDecodeError:
                                pass
                        
                        # Timeout after 5 seconds
                        if time.time() - response_start > 5:
                            break
                            
                except websockets.exceptions.ConnectionClosed:
                    pass
                
                # Calculate metrics
                end_time = time.time()
                rtt_ms = (end_time - start_time) * 1000
                latency_ms = (response_start - start_time) * 1000
                
                # Analyze audio quality
                if response_data:
                    response_audio = np.array(response_data, dtype=np.int16)
                    quality_metrics = self.analyze_audio_quality(response_audio)
                    frequency_response = self.analyze_frequency_response(response_audio)
                else:
                    quality_metrics = {"quality_score": 0.0, "snr": 0.0, "distortion": 1.0, "dynamic_range": 0.0}
                    frequency_response = {}
                
                # Determine if test passed
                test_passed = (
                    rtt_ms <= self.config.max_rtt_ms and
                    quality_metrics["quality_score"] >= 70.0 and
                    quality_metrics["snr"] >= 20.0 and
                    quality_metrics["distortion"] <= 0.1
                )
                
                metrics = AudioMetrics(
                    rtt_ms=rtt_ms,
                    latency_ms=latency_ms,
                    audio_quality_score=quality_metrics["quality_score"],
                    signal_to_noise_ratio=quality_metrics["snr"],
                    frequency_response=frequency_response,
                    distortion_level=quality_metrics["distortion"],
                    dynamic_range=quality_metrics["dynamic_range"],
                    test_passed=test_passed
                )
                
                print(f"📊 Roundtrip time: {rtt_ms:.1f}ms")
                print(f"📊 Latency: {latency_ms:.1f}ms")
                print(f"📊 Audio quality: {quality_metrics['quality_score']:.1f}/100")
                print(f"📊 SNR: {quality_metrics['snr']:.1f}dB")
                print(f"📊 Distortion: {quality_metrics['distortion']:.3f}")
                print(f"📊 Dynamic range: {quality_metrics['dynamic_range']:.1f}dB")
                
                if test_passed:
                    print("✅ Audio roundtrip test passed")
                else:
                    print("❌ Audio roundtrip test failed")
                    self.errors.append(f"Audio test failed: RTT={rtt_ms:.1f}ms, Quality={quality_metrics['quality_score']:.1f}")
                
                return metrics
                
        except Exception as e:
            print(f"❌ Audio roundtrip test failed: {e}")
            self.errors.append(f"Audio roundtrip failed: {e}")
            return None
    
    async def test_stability(self, num_connections: int = 5) -> bool:
        """Test connection stability with multiple concurrent connections"""
        print(f"🔄 Testing stability with {num_connections} concurrent connections...")
        
        async def single_connection_test(conn_id: int) -> bool:
            try:
                uri = f"{self.config.protocol}://{self.config.host}:{self.config.port}/ws"
                async with websockets.connect(uri, timeout=3) as websocket:
                    await websocket.send(json.dumps({
                        "type": "ping",
                        "connectionId": conn_id,
                        "timestamp": time.time()
                    }))
                    await asyncio.sleep(0.5)
                    return True
            except Exception as e:
                print(f"❌ Connection {conn_id} failed: {e}")
                return False
        
        # Run concurrent connections
        tasks = [single_connection_test(i) for i in range(num_connections)]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        successful = sum(1 for r in results if r is True)
        success_rate = successful / num_connections
        
        print(f"📊 Stability: {successful}/{num_connections} connections successful ({success_rate:.1%})")
        
        if success_rate >= 0.8:  # 80% success rate
            print("✅ Stability test passed")
            return True
        else:
            print("❌ Stability test failed")
            self.errors.append(f"Stability test failed: {success_rate:.1%} success rate")
            return False
    
    def generate_report(self) -> Dict[str, Any]:
        """Generate comprehensive test report"""
        report = {
            "timestamp": time.strftime("%Y-%m-%d %H:%M:%S"),
            "config": {
                "host": self.config.host,
                "port": self.config.port,
                "protocol": self.config.protocol,
                "max_rtt_ms": self.config.max_rtt_ms,
                "test_duration_ms": self.config.test_duration_ms,
                "sample_rate": self.config.sample_rate,
                "frequency": self.config.frequency
            },
            "results": [],
            "summary": {
                "total_tests": 0,
                "passed_tests": 0,
                "failed_tests": 0,
                "success_rate": 0.0,
                "average_rtt": 0.0,
                "average_quality": 0.0
            },
            "errors": self.errors
        }
        
        if self.results:
            report["results"] = [
                {
                    "rtt_ms": r.rtt_ms,
                    "latency_ms": r.latency_ms,
                    "audio_quality_score": r.audio_quality_score,
                    "signal_to_noise_ratio": r.signal_to_noise_ratio,
                    "distortion_level": r.distortion_level,
                    "dynamic_range": r.dynamic_range,
                    "test_passed": r.test_passed
                }
                for r in self.results
            ]
            
            report["summary"]["total_tests"] = len(self.results)
            report["summary"]["passed_tests"] = sum(1 for r in self.results if r.test_passed)
            report["summary"]["failed_tests"] = len(self.results) - report["summary"]["passed_tests"]
            report["summary"]["success_rate"] = report["summary"]["passed_tests"] / report["summary"]["total_tests"] * 100
            report["summary"]["average_rtt"] = sum(r.rtt_ms for r in self.results) / len(self.results)
            report["summary"]["average_quality"] = sum(r.audio_quality_score for r in self.results) / len(self.results)
        
        return report
    
    async def run_all_tests(self) -> bool:
        """Run all E2E tests"""
        print("🧪 Starting E2E WebSocket Audio Analyzer")
        print("=" * 50)
        print(f"Host: {self.config.host}:{self.config.port}")
        print(f"Protocol: {self.config.protocol}")
        print(f"Max RTT: {self.config.max_rtt_ms}ms")
        print(f"Test Duration: {self.config.test_duration_ms}ms")
        print(f"Frequency: {self.config.frequency}Hz")
        print("")
        
        # Test 1: Connection
        if not await self.test_connection():
            return False
        
        # Test 2: Audio Roundtrip
        metrics = await self.test_audio_roundtrip()
        if metrics:
            self.results.append(metrics)
        
        # Test 3: Stability
        await self.test_stability()
        
        # Generate report
        report = self.generate_report()
        
        print("\n📋 E2E Test Report")
        print("=" * 20)
        print(f"Total Tests: {report['summary']['total_tests']}")
        print(f"Passed: {report['summary']['passed_tests']}")
        print(f"Failed: {report['summary']['failed_tests']}")
        print(f"Success Rate: {report['summary']['success_rate']:.1f}%")
        
        if self.results:
            print(f"Average RTT: {report['summary']['average_rtt']:.1f}ms")
            print(f"Average Quality: {report['summary']['average_quality']:.1f}/100")
        
        if self.errors:
            print("\n❌ Errors:")
            for error in self.errors:
                print(f"   - {error}")
        
        # Save report
        report_path = Path(__file__).parent / "e2e-audio-report.json"
        with open(report_path, 'w') as f:
            json.dump(report, f, indent=2)
        print(f"\n📄 Report saved to: {report_path}")
        
        return report['summary']['success_rate'] >= 80.0

async def main():
    """Main entry point"""
    config = TestConfig()
    
    # Parse command line arguments
    if len(sys.argv) > 1:
        config.host = sys.argv[1]
    if len(sys.argv) > 2:
        config.port = int(sys.argv[2])
    
    analyzer = WebSocketAudioAnalyzer(config)
    success = await analyzer.run_all_tests()
    
    if success:
        print("\n🎉 E2E tests completed successfully!")
        sys.exit(0)
    else:
        print("\n💥 E2E tests failed!")
        sys.exit(1)

if __name__ == "__main__":
    asyncio.run(main())
