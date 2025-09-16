import React, { useState, useEffect, useRef } from 'react'
import { invoke } from '@tauri-apps/api/core'
import { emit, listen } from '@tauri-apps/api/event'

function App() {
  const [isRecording, setIsRecording] = useState(false)
  const [status, setStatus] = useState('Готов к работе')
  const [permission, setPermission] = useState<'unknown' | 'granted' | 'denied'>('unknown')
  const [isLogsWindow, setIsLogsWindow] = useState(false)
  const [logs, setLogs] = useState<string[]>([])
  const mediaRecorderRef = useRef<MediaRecorder | null>(null)
  const mediaStreamRef = useRef<MediaStream | null>(null)
  const wsRef = useRef<WebSocket | null>(null)

  const appendLog = (text: string) => {
    const line = `[${new Date().toLocaleTimeString()}] ${text}`
    if (isLogsWindow) {
      setLogs(prev => [...prev.slice(-200), line])
    }
    // Tauri events (best-effort)
    emit('sj-log', { line }).catch(() => {})
    // localStorage bridge (works across windows reliably)
    try { localStorage.setItem('sj-log-line', line) } catch {}
  }

  const setStatusAndLog = (text: string) => {
    setStatus(text)
    appendLog(text)
  }

  const withTimeout = async <T,>(promise: Promise<T>, ms: number, label: string): Promise<T> => {
    return new Promise<T>((resolve, reject) => {
      const t = setTimeout(() => reject(new Error(`${label}: timeout ${ms}ms`)), ms)
      promise
        .then((v) => { clearTimeout(t); resolve(v) })
        .catch((e) => { clearTimeout(t); reject(e) })
    })
  }

  useEffect(() => {
    try {
      const params = new URLSearchParams(window.location.search)
      if (params.get('window') === 'logs') {
        setIsLogsWindow(true)
      }
    } catch {}
  }, [])

  // Подписка на глобальные логи в окне логов
  useEffect(() => {
    if (!isLogsWindow) return
    let unlisten: (() => void) | undefined
    listen<{ line: string }>('sj-log', (e) => {
      const line = e.payload?.line ?? ''
      setLogs(prev => [...prev.slice(-200), line])
    }).then((f) => { unlisten = f as unknown as () => void }).catch(() => {})

    const onStorage = (e: StorageEvent) => {
      if (e.key === 'sj-log-line' && e.newValue) {
        setLogs(prev => [...prev.slice(-200), e.newValue!])
      }
    }
    window.addEventListener('storage', onStorage)
    return () => { try { unlisten && unlisten() } catch {} ; window.removeEventListener('storage', onStorage) }
  }, [isLogsWindow])

  useEffect(() => {
    const handler = async (e: KeyboardEvent) => {
      if (e.code === 'F9' || e.code === 'Space') {
        e.preventDefault()
        await startRecording()
      } else if (e.code === 'F10') {
        e.preventDefault()
        await stopRecording()
      }
    }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [])

  // Проверка доступа (native тест) с таймаутом
  useEffect(() => {
    const check = async () => {
      try {
        setStatusAndLog('Проверка доступа (native)...')
        await withTimeout(invoke('start_native_recording'), 10000, 'start_native_recording')
        const stopMsg = await withTimeout(invoke<string>('stop_native_recording'), 4000, 'stop_native_recording')
        setPermission('granted')
        setStatusAndLog(`Доступ к микрофону доступен (native). ${stopMsg}`)
      } catch (e) {
        setPermission('denied')
        setStatusAndLog(`Проверка не удалась: ${e}`)
      }
    }
    check().catch(() => setPermission('unknown'))
  }, [])

  const startRecording = async () => {
    try {
      setStatusAndLog('Запрос доступа к микрофону (native)...')
      const startMsg = await withTimeout(invoke<string>('start_native_recording'), 10000, 'start_native_recording')
      setIsRecording(true)
      setStatusAndLog(startMsg)
    } catch (nativeErr) {
      setStatusAndLog(`Native ошибка: ${nativeErr}`)
      setPermission('denied')
    }
  }

  const stopRecording = async () => {
    try {
      const stopMsg = await withTimeout(invoke<string>('stop_native_recording'), 4000, 'stop_native_recording')
      setStatusAndLog(stopMsg)
    } catch (e) {
      setStatusAndLog(`Остановка: ${e}`)
    }
    try {
      mediaRecorderRef.current?.state === 'recording' && mediaRecorderRef.current.stop()
      mediaStreamRef.current?.getTracks().forEach(t => t.stop())
      mediaRecorderRef.current = null
      mediaStreamRef.current = null
    } catch {}

    setIsRecording(false)
  }

  const requestPermission = async () => {
    try {
      setStatusAndLog('Проверяю доступ (native)...')
      await withTimeout(invoke('start_native_recording'), 10000, 'start_native_recording')
      const stopMsg = await withTimeout(invoke<string>('stop_native_recording'), 4000, 'stop_native_recording')
      setPermission('granted')
      setStatusAndLog(`Доступ к микрофону разрешён (native). ${stopMsg}`)
    } catch (e) {
      setPermission('denied')
      setStatusAndLog(`Доступ к микрофону отклонён (native): ${e}`)
    }
  }

  const probeDevices = async () => {
    try {
      const res = await withTimeout(invoke<string>('probe_cpal_devices'), 3000, 'probe_cpal_devices')
      setStatusAndLog(`Аудио устройства: ${res}`)
    } catch (e) {
      setStatusAndLog(`Проверка устройств не удалась: ${e}`)
    }
  }

  if (isLogsWindow) {
    return (
      <div className="w-full h-screen bg-black text-green-400 font-mono text-sm p-4">
        <div className="mb-2 text-white">SmartJARVIS Logs</div>
        <div className="w-full h-[90%] overflow-auto border border-gray-700 rounded p-2 bg-gray-900">
          {logs.map((l, i) => (
            <div key={i}>{l}</div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="w-full h-screen bg-black text-white flex flex-col items-center justify-center">
      <button
        onClick={isRecording ? stopRecording : startRecording}
        className={`w-32 h-32 rounded-full border-4 transition-all duration-300 ${
          isRecording 
            ? 'bg-red-600 border-red-400 shadow-red-500/50' 
            : 'bg-green-600 border-green-400 shadow-green-500/50'
        } shadow-2xl hover:scale-105 active:scale-95`}
      >
        <div className="text-4xl">
          {isRecording ? '⏹️' : '🎤'}
        </div>
      </button>

      <div className="mt-8 text-center">
        <div className="text-xl font-semibold mb-2">
          {isRecording ? 'Запись...' : 'SmartJARVIS'}
        </div>
        <div className="text-sm text-gray-400">
          {status}
        </div>
        <div className="mt-3 flex items-center gap-2 justify-center">
          {permission !== 'granted' && (
            <button onClick={requestPermission} className="px-3 py-1 text-xs bg-gray-700 hover:bg-gray-600 rounded">Разрешить микрофон (native)</button>
          )}
          <button onClick={probeDevices} className="px-3 py-1 text-xs bg-gray-700 hover:bg-gray-600 rounded">Проверить устройства</button>
        </div>
      </div>

      <div className="mt-8 text-xs text-gray-500 text-center">
        <div>F9 или Space - начать запись</div>
        <div>F10 - остановить запись</div>
      </div>

      {isRecording && (
        <div className="mt-4 flex items-center space-x-2">
          <div className="w-3 h-3 bg-red-500 rounded-full animate-pulse"></div>
          <span className="text-sm">Запись активна</span>
        </div>
      )}
    </div>
  )
}

export default App