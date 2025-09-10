import { useState, useCallback, useRef, useEffect } from 'react'

interface VoiceConnectionState {
  isConnected: boolean
  isRecording: boolean
  audioLevel: number
  lastResponse: string | null
  error: string | null
}

export const useVoiceConnection = () => {
  const [state, setState] = useState<VoiceConnectionState>({
    isConnected: false,
    isRecording: false,
    audioLevel: 0,
    lastResponse: null,
    error: null
  })

  const wsRef = useRef<WebSocket | null>(null)
  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const audioContextRef = useRef<AudioContext | null>(null)
  const analyserRef = useRef<AnalyserNode | null>(null)
  const animationFrameRef = useRef<number | null>(null)

  // Connect to WebSocket
  const connect = useCallback(() => {
    try {
      const wsUrl = `ws://localhost:8080/voice`
      console.log('Connecting to:', wsUrl)
      
      const ws = new WebSocket(wsUrl)
      
      ws.onopen = () => {
        console.log('WebSocket connected')
        setState(prev => ({ ...prev, isConnected: true, error: null }))
      }
      
      ws.onmessage = (event) => {
        console.log('WebSocket message:', event.data)
        setState(prev => ({ ...prev, lastResponse: event.data }))
      }
      
      ws.onclose = () => {
        console.log('WebSocket disconnected')
        setState(prev => ({ ...prev, isConnected: false }))
        
        // Auto-reconnect after 3 seconds
        setTimeout(() => {
          if (wsRef.current === ws) {
            connect()
          }
        }, 3000)
      }
      
      ws.onerror = (error) => {
        console.error('WebSocket error:', error)
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

      // Setup MediaRecorder
      const mediaRecorder = new MediaRecorder(stream, {
        mimeType: 'audio/webm;codecs=opus'
      })

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0 && wsRef.current?.readyState === WebSocket.OPEN) {
          wsRef.current.send(event.data)
        }
      }

      mediaRecorder.onstop = () => {
        stream.getTracks().forEach(track => track.stop())
        setState(prev => ({ ...prev, isRecording: false, audioLevel: 0 }))
        
        if (animationFrameRef.current) {
          cancelAnimationFrame(animationFrameRef.current)
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

  return {
    isConnected: state.isConnected,
    isRecording: state.isRecording,
    audioLevel: state.audioLevel,
    lastResponse: state.lastResponse,
    error: state.error,
    connect,
    disconnect,
    startRecording,
    stopRecording
  }
}
