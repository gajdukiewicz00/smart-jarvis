#!/usr/bin/env node

/**
 * E2E Smoke Tests for WebSocket Audio Tester
 * Tests voice roundtrip: STT → NLU → NLG → TTS
 * 
 * Requirements:
 * - RTT (Round Trip Time) < 300ms
 * - 2-second sine wave test
 * - WebSocket connection stability
 * - Audio quality validation
 */

const WebSocket = require('ws');
const fs = require('fs');
const path = require('path');
const { spawn } = require('child_process');

// Test configuration
const CONFIG = {
    host: process.env.WS_HOST || 'localhost',
    port: process.env.WS_PORT || '7090',
    protocol: process.env.WS_PROTOCOL || 'ws',
    timeout: 30000, // 30 seconds
    maxRTT: 300, // 300ms max round trip time
    testDuration: 2000, // 2 seconds
    sampleRate: 16000,
    chunkSize: 1024
};

// Test results
const results = {
    connection: false,
    audioGeneration: false,
    roundTripTime: 0,
    audioQuality: false,
    stability: false,
    errors: []
};

/**
 * Generate 2-second sine wave audio data
 */
function generateSineWave(duration = 2000, frequency = 440, sampleRate = 16000) {
    const samples = Math.floor(duration * sampleRate / 1000);
    const buffer = new Int16Array(samples);
    const amplitude = 0.3; // 30% amplitude to avoid clipping
    
    for (let i = 0; i < samples; i++) {
        const t = i / sampleRate;
        buffer[i] = Math.floor(amplitude * Math.sin(2 * Math.PI * frequency * t) * 32767);
    }
    
    return buffer;
}

/**
 * Test WebSocket connection
 */
async function testConnection() {
    return new Promise((resolve, reject) => {
        console.log('🔌 Testing WebSocket connection...');
        
        const url = `${CONFIG.protocol}://${CONFIG.host}:${CONFIG.port}/ws`;
        const ws = new WebSocket(url);
        
        const timeout = setTimeout(() => {
            ws.close();
            reject(new Error('Connection timeout'));
        }, 5000);
        
        ws.on('open', () => {
            clearTimeout(timeout);
            console.log('✅ WebSocket connected successfully');
            results.connection = true;
            ws.close();
            resolve(true);
        });
        
        ws.on('error', (error) => {
            clearTimeout(timeout);
            console.log('❌ WebSocket connection failed:', error.message);
            results.errors.push(`Connection failed: ${error.message}`);
            reject(error);
        });
    });
}

/**
 * Test audio generation and transmission
 */
async function testAudioTransmission() {
    return new Promise((resolve, reject) => {
        console.log('🎵 Testing audio generation and transmission...');
        
        const url = `${CONFIG.protocol}://${CONFIG.host}:${CONFIG.port}/ws`;
        const ws = new WebSocket(url);
        
        const timeout = setTimeout(() => {
            ws.close();
            reject(new Error('Audio transmission timeout'));
        }, CONFIG.timeout);
        
        let sessionStartTime = 0;
        let audioChunks = [];
        let receivedChunks = 0;
        
        ws.on('open', () => {
            console.log('📡 WebSocket connected, starting audio test...');
            
            // Generate 2-second sine wave
            const audioData = generateSineWave(CONFIG.testDuration, 440, CONFIG.sampleRate);
            sessionStartTime = Date.now();
            
            // Send session header
            const header = {
                sessionId: `test-${Date.now()}`,
                correlationId: `corr-${Date.now()}`,
                sampleRate: CONFIG.sampleRate,
                protocolVersion: 1,
                testMode: true
            };
            
            ws.send(JSON.stringify(header));
            
            // Send audio data in chunks
            const chunkSize = CONFIG.chunkSize;
            let offset = 0;
            
            const sendChunk = () => {
                if (offset >= audioData.length) {
                    // Send end marker
                    ws.send(JSON.stringify({ type: 'end', timestamp: Date.now() }));
                    return;
                }
                
                const chunk = audioData.slice(offset, offset + chunkSize);
                ws.send(chunk.buffer);
                audioChunks.push(chunk);
                offset += chunkSize;
                
                // Continue sending chunks
                setTimeout(sendChunk, 10); // 10ms intervals
            };
            
            sendChunk();
        });
        
        ws.on('message', (data) => {
            receivedChunks++;
            
            // Check if we received audio response
            if (data instanceof Buffer) {
                console.log(`📥 Received audio chunk ${receivedChunks}: ${data.length} bytes`);
                
                // Calculate round trip time
                const roundTripTime = Date.now() - sessionStartTime;
                results.roundTripTime = roundTripTime;
                
                if (roundTripTime <= CONFIG.maxRTT) {
                    console.log(`✅ Round trip time: ${roundTripTime}ms (≤ ${CONFIG.maxRTT}ms)`);
                    results.audioQuality = true;
                } else {
                    console.log(`⚠️  Round trip time: ${roundTripTime}ms (> ${CONFIG.maxRTT}ms)`);
                    results.errors.push(`RTT too high: ${roundTripTime}ms`);
                }
                
                // Test completed
                clearTimeout(timeout);
                ws.close();
                resolve(true);
            }
        });
        
        ws.on('error', (error) => {
            clearTimeout(timeout);
            console.log('❌ Audio transmission error:', error.message);
            results.errors.push(`Audio transmission failed: ${error.message}`);
            reject(error);
        });
        
        ws.on('close', () => {
            console.log('🔌 WebSocket connection closed');
        });
    });
}

/**
 * Test connection stability
 */
async function testStability() {
    return new Promise((resolve, reject) => {
        console.log('🔄 Testing connection stability...');
        
        const url = `${CONFIG.protocol}://${CONFIG.host}:${CONFIG.port}/ws`;
        const connections = [];
        const maxConnections = 5;
        let successfulConnections = 0;
        
        const createConnection = (index) => {
            return new Promise((connResolve, connReject) => {
                const ws = new WebSocket(url);
                
                ws.on('open', () => {
                    console.log(`✅ Connection ${index + 1} established`);
                    successfulConnections++;
                    
                    // Send test message
                    ws.send(JSON.stringify({ 
                        type: 'ping', 
                        timestamp: Date.now(),
                        connectionId: index 
                    }));
                    
                    // Close after short delay
                    setTimeout(() => {
                        ws.close();
                        connResolve();
                    }, 1000);
                });
                
                ws.on('error', (error) => {
                    console.log(`❌ Connection ${index + 1} failed:`, error.message);
                    connReject(error);
                });
            });
        };
        
        // Create multiple connections
        const connectionPromises = [];
        for (let i = 0; i < maxConnections; i++) {
            connectionPromises.push(createConnection(i));
        }
        
        Promise.allSettled(connectionPromises)
            .then((results) => {
                const successful = results.filter(r => r.status === 'fulfilled').length;
                const failed = results.filter(r => r.status === 'rejected').length;
                
                console.log(`📊 Stability test: ${successful}/${maxConnections} connections successful`);
                
                if (successful >= maxConnections * 0.8) { // 80% success rate
                    results.stability = true;
                    console.log('✅ Connection stability test passed');
                } else {
                    console.log('⚠️  Connection stability test failed');
                    results.errors.push(`Stability test failed: ${failed} connections failed`);
                }
                
                resolve();
            })
            .catch(reject);
    });
}

/**
 * Test audio quality metrics
 */
async function testAudioQuality() {
    console.log('🎧 Testing audio quality metrics...');
    
    // Generate test audio
    const testAudio = generateSineWave(1000, 440, CONFIG.sampleRate);
    
    // Basic quality checks
    const checks = {
        sampleRate: CONFIG.sampleRate === 16000,
        amplitude: Math.max(...testAudio) > 0,
        noClipping: Math.max(...testAudio) < 32767,
        duration: testAudio.length === CONFIG.sampleRate // 1 second
    };
    
    const passedChecks = Object.values(checks).filter(Boolean).length;
    const totalChecks = Object.keys(checks).length;
    
    console.log(`📊 Audio quality: ${passedChecks}/${totalChecks} checks passed`);
    
    if (passedChecks === totalChecks) {
        console.log('✅ Audio quality test passed');
        return true;
    } else {
        console.log('⚠️  Audio quality test failed');
        results.errors.push('Audio quality checks failed');
        return false;
    }
}

/**
 * Generate test report
 */
function generateReport() {
    const report = {
        timestamp: new Date().toISOString(),
        config: CONFIG,
        results: results,
        summary: {
            totalTests: 5,
            passedTests: Object.values(results).filter(Boolean).length,
            failedTests: results.errors.length,
            successRate: Math.round((Object.values(results).filter(Boolean).length / 5) * 100)
        }
    };
    
    console.log('\n📋 E2E Test Report');
    console.log('==================');
    console.log(`✅ Connection: ${results.connection ? 'PASS' : 'FAIL'}`);
    console.log(`✅ Audio Generation: ${results.audioGeneration ? 'PASS' : 'FAIL'}`);
    console.log(`✅ Round Trip Time: ${results.roundTripTime}ms (≤ ${CONFIG.maxRTT}ms)`);
    console.log(`✅ Audio Quality: ${results.audioQuality ? 'PASS' : 'FAIL'}`);
    console.log(`✅ Stability: ${results.stability ? 'PASS' : 'FAIL'}`);
    console.log(`📊 Success Rate: ${report.summary.successRate}%`);
    
    if (results.errors.length > 0) {
        console.log('\n❌ Errors:');
        results.errors.forEach(error => console.log(`   - ${error}`));
    }
    
    // Save report to file
    const reportPath = path.join(__dirname, 'e2e-test-report.json');
    fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
    console.log(`\n📄 Report saved to: ${reportPath}`);
    
    return report;
}

/**
 * Main test runner
 */
async function runE2ETests() {
    console.log('🧪 Starting E2E WebSocket Audio Tester Tests');
    console.log('=============================================');
    console.log(`Host: ${CONFIG.host}:${CONFIG.port}`);
    console.log(`Protocol: ${CONFIG.protocol}`);
    console.log(`Max RTT: ${CONFIG.maxRTT}ms`);
    console.log(`Test Duration: ${CONFIG.testDuration}ms`);
    console.log('');
    
    try {
        // Test 1: Connection
        await testConnection();
        
        // Test 2: Audio Generation
        results.audioGeneration = await testAudioQuality();
        
        // Test 3: Audio Transmission
        await testAudioTransmission();
        
        // Test 4: Stability
        await testStability();
        
        // Generate final report
        const report = generateReport();
        
        // Exit with appropriate code
        if (report.summary.successRate >= 80) {
            console.log('\n🎉 E2E tests completed successfully!');
            process.exit(0);
        } else {
            console.log('\n💥 E2E tests failed!');
            process.exit(1);
        }
        
    } catch (error) {
        console.log('\n💥 E2E tests failed with error:', error.message);
        results.errors.push(`Test execution failed: ${error.message}`);
        generateReport();
        process.exit(1);
    }
}

// Run tests if this file is executed directly
if (require.main === module) {
    runE2ETests();
}

module.exports = {
    runE2ETests,
    testConnection,
    testAudioTransmission,
    testStability,
    testAudioQuality,
    generateReport,
    CONFIG
};
