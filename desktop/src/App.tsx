import React, { useState, useEffect } from 'react'
import { Canvas } from '@react-three/fiber'
import { motion, AnimatePresence } from 'framer-motion'
import VoiceInterface from './components/VoiceInterface'
import HUD3D from './components/HUD3D'
import ServiceStatus from './components/ServiceStatus'
import TodoManager from './components/TodoManager'
import AudioVisualizer from './components/AudioVisualizer'
import WakeWordIndicator from './components/WakeWordIndicator'
import { useVoiceConnection } from './hooks/useVoiceConnection'
import { useServiceStatus } from './hooks/useServiceStatus'
import { Mic, MicOff, Settings, List, Home } from 'lucide-react'

type ViewMode = 'hud' | 'todos' | 'settings'

function App() {
  const [viewMode, setViewMode] = useState<ViewMode>('hud')
  const [isListening, setIsListening] = useState(false)
  const [isWakeWordActive, setIsWakeWordActive] = useState(false)
  
  const { 
    isConnected, 
    isRecording, 
    audioLevel,
    lastResponse,
    isTtsActive,
    bargeInTriggered,
    connect,
    disconnect,
    startRecording,
    stopRecording 
  } = useVoiceConnection()
  
  const { services, overallHealth } = useServiceStatus()

  useEffect(() => {
    // Auto-connect on mount
    connect()
    return () => disconnect()
  }, [connect, disconnect])

  const handleVoiceToggle = () => {
    if (isRecording) {
      stopRecording()
      setIsListening(false)
    } else {
      startRecording()
      setIsListening(true)
    }
  }

  return (
    <div className="w-full h-screen bg-black text-white overflow-y-auto overflow-x-hidden">
      {/* Background HUD only on HUD view to avoid blocking scroll elsewhere */}
      {viewMode === 'hud' && (
        <div className="fixed inset-0 z-0 pointer-events-none select-none">
          <Canvas camera={{ position: [0, 0, 10], fov: 60 }}>
            <HUD3D 
              services={services}
              isRecording={isRecording}
              audioLevel={audioLevel}
              overallHealth={overallHealth}
            />
          </Canvas>
        </div>
      )}

      {/* Top Bar */}
      <div className="sticky top-0 left-0 right-0 z-20 flex justify-between items-center px-4 py-4 bg-black/40 backdrop-blur-md">
        <div className="flex items-center space-x-4">
          <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent">
            SmartJARVIS
          </h1>
          <div className={`service-status ${overallHealth}`}>
            {services.filter(s => s.status === 'healthy').length}/{services.length} сервисов
          </div>
        </div>
        
        <div className="flex items-center space-x-2">
          <button
            onClick={() => setViewMode('hud')}
            className={`p-2 rounded-lg transition-colors ${
              viewMode === 'hud' ? 'bg-blue-500/30 text-blue-400' : 'hover:bg-white/10'
            }`}
          >
            <Home size={20} />
          </button>
          <button
            onClick={() => setViewMode('todos')}
            className={`p-2 rounded-lg transition-colors ${
              viewMode === 'todos' ? 'bg-blue-500/30 text-blue-400' : 'hover:bg-white/10'
            }`}
          >
            <List size={20} />
          </button>
          <button
            onClick={() => setViewMode('settings')}
            className={`p-2 rounded-lg transition-colors ${
              viewMode === 'settings' ? 'bg-blue-500/30 text-blue-400' : 'hover:bg-white/10'
            }`}
          >
            <Settings size={20} />
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="relative z-10 flex flex-col pb-24 pt-4">
        <div className="w-full max-w-6xl mx-auto px-6">
          <AnimatePresence mode="wait">
            {viewMode === 'hud' && (
              <motion.div
                key="hud"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                className="text-center flex flex-col items-center"
              >
                {/* Central Voice Interface */}
                <VoiceInterface
                  isConnected={isConnected}
                  isRecording={isRecording}
                  audioLevel={audioLevel}
                  isTtsActive={isTtsActive}
                  bargeInTriggered={bargeInTriggered}
                  onToggleRecording={handleVoiceToggle}
                />
                
                {/* Last Response */}
                {lastResponse && (
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="mt-8 max-w-md mx-auto"
                  >
                    <div className="bg-white/10 backdrop-blur-sm rounded-lg p-4 border border-white/20">
                      <p className="text-sm text-gray-300">Последний ответ:</p>
                      <p className="text-white">{lastResponse}</p>
                    </div>
                  </motion.div>
                )}
              </motion.div>
            )}
            
            {viewMode === 'todos' && (
              <motion.div
                key="todos"
                initial={{ opacity: 0, x: 100 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -100 }}
                className="w-full max-w-4xl mx-auto p-6"
              >
                <TodoManager />
              </motion.div>
            )}
            
            {viewMode === 'settings' && (
              <motion.div
                key="settings"
                initial={{ opacity: 0, x: 100 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -100 }}
                className="w-full max-w-4xl mx-auto p-6"
              >
                <div className="space-y-6 pr-1">
                  <div className="flex items-center justify-between mb-2">
                    <h2 className="text-xl font-semibold">Статус и настройки</h2>
                    <button
                      onClick={() => {
                        const el = document.getElementById('wake-word');
                        if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
                      }}
                      className="text-sm px-3 py-1 rounded bg-blue-600 hover:bg-blue-700"
                    >
                      К блоку Wake Word
                    </button>
                  </div>
                  <ServiceStatus services={services} />
                  <div id="wake-word">
                    <WakeWordIndicator 
                      isActive={isWakeWordActive}
                      onToggle={setIsWakeWordActive}
                    />
                  </div>
                  <div id="bottom-anchor" />
                </div>
              </motion.div>
            )}
          </AnimatePresence>
        </div>

        {/* Bottom Audio Visualizer */}
        {isRecording && (
          <div className="absolute bottom-4 left-4 right-4 z-20">
            <AudioVisualizer audioLevel={audioLevel} isActive={isRecording} />
          </div>
        )}
      </div>

      {/* Connection Status */}
      <div className="absolute bottom-4 right-4 z-20">
        <div className={`flex items-center space-x-2 px-3 py-2 rounded-lg backdrop-blur-sm ${
          isConnected ? 'bg-green-500/20 border border-green-500/30' : 'bg-red-500/20 border border-red-500/30'
        }`}>
          {isConnected ? (
            <Mic className="w-4 h-4 text-green-400" />
          ) : (
            <MicOff className="w-4 h-4 text-red-400" />
          )}
          <span className="text-xs">
            {isConnected ? 'Подключено' : 'Отключено'}
          </span>
        </div>
      </div>

      {/* Quick scroll buttons */}
      {viewMode === 'settings' && (
        <div className="fixed bottom-4 left-4 z-20 flex flex-col space-y-2">
          <button
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
            className="px-3 py-2 text-xs rounded bg-gray-700 hover:bg-gray-600"
          >
            Вверх
          </button>
          <button
            onClick={() => {
              const el = document.getElementById('bottom-anchor');
              if (el) el.scrollIntoView({ behavior: 'smooth', block: 'end' });
            }}
            className="px-3 py-2 text-xs rounded bg-gray-700 hover:bg-gray-600"
          >
            Вниз
          </button>
        </div>
      )}
    </div>
  )
}

export default App
