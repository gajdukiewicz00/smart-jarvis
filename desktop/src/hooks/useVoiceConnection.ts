import { useState, useCallback, useRef, useEffect } from 'react'

interface VoiceConnectionState {
  isConnected: boolean
  isRecording: boolean
  audioLevel: number
  lastResponse: string | null
  error: string | null
  isTtsActive: boolean
  bargeInTriggered: boolean
}

export const useVoiceConnection = () => {
  const [state, setState] = useState<VoiceConnectionState>({
    isConnected: false,
    isRecording: false,
    audioLevel: 0,
    lastResponse: null,
    error: null,
    isTtsActive: false,
    bargeInTriggered: false
  })

  const wsRef = useRef<WebSocket | null>(null)
  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const audioContextRef = useRef<AudioContext | null>(null)
  const analyserRef = useRef<AnalyserNode | null>(null)
  const animationFrameRef = useRef<number | null>(null)

  // Connect to WebSocket
  const connect = useCallback(() => {
    try {
      // Принудительно используем HTTP для WebSocket (избегаем проблем с самоподписанным сертификатом)
      const wsUrl = 'ws://localhost:3000/voice'
      console.log('Connecting to:', wsUrl)
      
      const ws = new WebSocket(wsUrl)
      
      ws.onopen = () => {
        console.log('WebSocket connected')
        setState(prev => ({ ...prev, isConnected: true, error: null }))
      }
      
      ws.onmessage = (event) => {
        console.log('WebSocket message:', event.data)
        
        try {
          // Try to parse as JSON for control messages
          const data = JSON.parse(event.data)
          
          if (data.type === 'tts_start') {
            setState(prev => ({ ...prev, isTtsActive: true }))
            playTtsStartSound()
          } else if (data.type === 'tts_stop') {
            setState(prev => ({ ...prev, isTtsActive: false }))
            playTtsStopSound()
          } else if (data.type === 'barge_in') {
            setState(prev => ({ 
              ...prev, 
              isTtsActive: false, 
              bargeInTriggered: true,
              lastResponse: 'Перебивка зафиксирована'
            }))
            playBargeInSound()
            
            // Clear barge-in flag after 2 seconds
            setTimeout(() => {
              setState(prev => ({ ...prev, bargeInTriggered: false }))
            }, 2000)
          }
        } catch {
          // Plain text response
          setState(prev => ({ ...prev, lastResponse: event.data }))
        }
      }
      
      ws.onclose = (event) => {
        console.log('WebSocket disconnected, code:', event.code, 'reason:', event.reason)
        setState(prev => ({ ...prev, isConnected: false }))
        
        // Auto-reconnect after 1 second
        setTimeout(() => {
          if (wsRef.current === ws) {
            console.log('Attempting to reconnect...')
            connect()
          }
        }, 1000)
      }
      
      ws.onerror = (error) => {
        console.error('WebSocket error:', error)
        console.error('WebSocket readyState:', ws.readyState)
        setState(prev => ({ ...prev, error: 'Connection failed' }))
      }
      
      wsRef.current = ws
    } catch (error) {
      console.error('Failed to connect:', error)
      setState(prev => ({ ...prev, error: 'Failed to connect' }))
    }
  }, [])

  // Disconnect WebSocket
  const disconnect = useCallback(() => {
    if (wsRef.current) {
      wsRef.current.close()
      wsRef.current = null
    }
    setState(prev => ({ ...prev, isConnected: false }))
  }, [])

  // Start audio recording
  const startRecording = useCallback(async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ 
        audio: {
          echoCancellation: true,
          noiseSuppression: true,
          sampleRate: 16000
        } 
      })

      // Setup audio context for visualization
      const audioContext = new AudioContext()
      const analyser = audioContext.createAnalyser()
      const source = audioContext.createMediaStreamSource(stream)
      
      analyser.fftSize = 256
      analyser.smoothingTimeConstant = 0.8
      source.connect(analyser)
      
      audioContextRef.current = audioContext
      analyserRef.current = analyser

      // Setup MediaRecorder with fallback format
      let mimeType = 'audio/webm;codecs=opus'
      if (!MediaRecorder.isTypeSupported(mimeType)) {
        mimeType = 'audio/webm'
        if (!MediaRecorder.isTypeSupported(mimeType)) {
          mimeType = 'audio/mp4'
        }
      }
      
      const mediaRecorder = new MediaRecorder(stream, {
        mimeType: mimeType
      })

      mediaRecorder.ondataavailable = (event) => {
        console.log('Audio data available:', event.data.size, 'bytes')
        if (event.data.size > 0) {
          if (wsRef.current?.readyState === WebSocket.OPEN) {
            console.log('Sending audio data to WebSocket')
            try {
              wsRef.current.send(event.data)
              // После отправки данных переподключаемся для следующего аудио
              setTimeout(() => {
                if (wsRef.current?.readyState === WebSocket.OPEN) {
                  wsRef.current.close()
                }
                connect()
              }, 100)
            } catch (error) {
              console.error('Error sending audio data:', error)
              // Попробуем переподключиться
              connect()
            }
          } else {
            console.log('WebSocket not ready, state:', wsRef.current?.readyState)
            // Попробуем переподключиться
            connect()
          }
        }
      }
      
      mediaRecorder.onstop = () => {
        stream.getTracks().forEach(track => track.stop())
        setState(prev => ({ ...prev, isRecording: false, audioLevel: 0 }))
        
        if (animationFrameRef.current) {
          cancelAnimationFrame(animationFrameRef.current)
        }
        
        // Отправляем сигнал окончания записи, но не закрываем соединение
        if (wsRef.current?.readyState === WebSocket.OPEN) {
          wsRef.current.send(JSON.stringify({ type: 'audio_end' }))
        }
      }

      mediaRecorderRef.current = mediaRecorder
      mediaRecorder.start(100) // Send chunks every 100ms

      setState(prev => ({ ...prev, isRecording: true }))
      
      // Start audio level monitoring
      const updateAudioLevel = () => {
        if (analyserRef.current) {
          const dataArray = new Uint8Array(analyserRef.current.frequencyBinCount)
          analyserRef.current.getByteFrequencyData(dataArray)
          
          // Calculate average volume
          const average = dataArray.reduce((sum, value) => sum + value, 0) / dataArray.length
          const normalizedLevel = average / 255
          
          setState(prev => ({ ...prev, audioLevel: normalizedLevel }))
        }
        
        if (state.isRecording) {
          animationFrameRef.current = requestAnimationFrame(updateAudioLevel)
        }
      }
      
      updateAudioLevel()

    } catch (error) {
      console.error('Failed to start recording:', error)
      setState(prev => ({ 
        ...prev, 
        error: 'Microphone access denied' 
      }))
    }
  }, [state.isRecording])

  // Stop audio recording
  const stopRecording = useCallback(() => {
    if (mediaRecorderRef.current && state.isRecording) {
      mediaRecorderRef.current.stop()
      mediaRecorderRef.current = null
    }
    
    if (audioContextRef.current) {
      audioContextRef.current.close()
      audioContextRef.current = null
    }
    
    if (animationFrameRef.current) {
      cancelAnimationFrame(animationFrameRef.current)
      animationFrameRef.current = null
    }
  }, [state.isRecording])

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      disconnect()
      stopRecording()
    }
  }, [disconnect, stopRecording])

  // Audio feedback functions
  const playTone = (frequency: number, duration: number) => {
    try {
      const audioContext = new AudioContext()
      const oscillator = audioContext.createOscillator()
      const gainNode = audioContext.createGain()
      
      oscillator.connect(gainNode)
      gainNode.connect(audioContext.destination)
      
      oscillator.frequency.setValueAtTime(frequency, audioContext.currentTime)
      oscillator.type = 'sine'
      
      gainNode.gain.setValueAtTime(0.1, audioContext.currentTime)
      gainNode.gain.exponentialRampToValueAtTime(0.001, audioContext.currentTime + duration / 1000)
      
      oscillator.start(audioContext.currentTime)
      oscillator.stop(audioContext.currentTime + duration / 1000)
    } catch (error) {
      console.warn('Audio feedback failed:', error)
    }
  }

  const playTtsStartSound = () => playTone(400, 100)
  const playTtsStopSound = () => playTone(200, 150)
  const playBargeInSound = () => playTone(800, 80)

  return {
    isConnected: state.isConnected,
    isRecording: state.isRecording,
    audioLevel: state.audioLevel,
    lastResponse: state.lastResponse,
    error: state.error,
    isTtsActive: state.isTtsActive,
    bargeInTriggered: state.bargeInTriggered,
    connect,
    disconnect,
    startRecording,
    stopRecording
  }
}

