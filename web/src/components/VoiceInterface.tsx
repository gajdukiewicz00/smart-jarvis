import React from 'react'
import { motion } from 'framer-motion'
import { Mic, MicOff, Wifi, WifiOff } from 'lucide-react'

interface VoiceInterfaceProps {
  isConnected: boolean
  isRecording: boolean
  audioLevel: number
  onToggleRecording: () => void
}

const VoiceInterface: React.FC<VoiceInterfaceProps> = ({
  isConnected,
  isRecording,
  audioLevel,
  onToggleRecording
}) => {
  const getButtonState = () => {
    if (!isConnected) return 'disconnected'
    if (isRecording) return 'recording'
    return 'ready'
  }

  const buttonState = getButtonState()

  return (
    <div className="flex flex-col items-center space-y-6">
      {/* Connection Status */}
      <div className="flex items-center space-x-2 text-sm">
        {isConnected ? (
          <>
            <Wifi className="w-4 h-4 text-green-400" />
            <span className="text-green-400">Голосовой шлюз подключен</span>
          </>
        ) : (
          <>
            <WifiOff className="w-4 h-4 text-red-400" />
            <span className="text-red-400">Подключение...</span>
          </>
        )}
      </div>

      {/* Voice Button */}
      <motion.button
        onClick={onToggleRecording}
        disabled={!isConnected}
        className={`voice-button ${buttonState}`}
        whileHover={{ scale: isConnected ? 1.1 : 1 }}
        whileTap={{ scale: isConnected ? 0.95 : 1 }}
        animate={{
          scale: isRecording ? [1, 1.05, 1] : 1,
          boxShadow: isRecording 
            ? `0 0 ${20 + audioLevel * 50}px rgba(239, 68, 68, 0.6)`
            : '0 0 10px rgba(59, 130, 246, 0.3)'
        }}
        transition={{
          scale: { duration: 0.5, repeat: isRecording ? Infinity : 0 },
          boxShadow: { duration: 0.1 }
        }}
      >
        {isConnected ? (
          isRecording ? (
            <MicOff className="w-8 h-8 text-white" />
          ) : (
            <Mic className="w-8 h-8 text-white" />
          )
        ) : (
          <div className="w-8 h-8 border-2 border-white/30 border-t-white rounded-full animate-spin" />
        )}
      </motion.button>

      {/* Instructions */}
      <div className="text-center space-y-2">
        <p className="text-lg font-medium">
          {buttonState === 'disconnected' && 'Подключение к сервису...'}
          {buttonState === 'ready' && 'Нажмите для записи команды'}
          {buttonState === 'recording' && 'Говорите... (нажмите для остановки)'}
        </p>
        
        {buttonState === 'ready' && (
          <p className="text-sm text-gray-400">
            Или скажите "Джарвис" для активации
          </p>
        )}
        
        {isRecording && (
          <div className="flex items-center justify-center space-x-2">
            <div className="w-2 h-2 bg-red-500 rounded-full animate-pulse" />
            <span className="text-sm text-red-400">Запись...</span>
            <div className="text-xs text-gray-400">
              Уровень: {Math.round(audioLevel * 100)}%
            </div>
          </div>
        )}
      </div>

      {/* Quick Commands */}
      {buttonState === 'ready' && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="grid grid-cols-2 gap-2 text-xs text-gray-400"
        >
          <div>"Покажи задачи"</div>
          <div>"Включи свет"</div>
          <div>"Сделай скриншот"</div>
          <div>"Помощь"</div>
        </motion.div>
      )}
    </div>
  )
}

export default VoiceInterface
