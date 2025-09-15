import React, { useState, useEffect } from 'react';
import { invoke } from '@tauri-apps/api/core';

interface WakeWordIndicatorProps {
  isActive: boolean;
  onToggle: (active: boolean) => void;
}

const WakeWordIndicator: React.FC<WakeWordIndicatorProps> = ({ isActive, onToggle }) => {
  const [status, setStatus] = useState<string>('Stopped');
  const [sensitivity, setSensitivity] = useState<number>(0.5);
  const [useLocal, setUseLocal] = useState<boolean>(true);
  const [useCloud, setUseCloud] = useState<boolean>(false);

  useEffect(() => {
    // Ensure detector exists on mount
    (async () => {
      try {
        const status = await invoke<string>('get_wake_word_status');
        if (status.includes('not created')) {
          await invoke<string>('create_wake_word_detector', {
            sensitivity,
            useLocal,
            useCloud,
            cloudApiKey: null,
            wakeWords: ['jarvis']
          });
        }
      } catch (e) {
        console.error('init wake word detector failed', e);
      } finally {
        updateStatus();
      }
    })();
  }, [isActive]);

  const updateStatus = async () => {
    try {
      const result = await invoke<string>('get_wake_word_status');
      setStatus(result);
    } catch (error) {
      console.error('Failed to get wake word status:', error);
      setStatus('Error');
    }
  };

  const handleStart = async () => {
    try {
      await invoke<string>('start_wake_word_detection');
      setStatus('Running');
      onToggle(true);
    } catch (error) {
      console.error('Failed to start wake word detection:', error);
    }
  };

  const handleStop = async () => {
    try {
      await invoke<string>('stop_wake_word_detection');
      setStatus('Stopped');
      onToggle(false);
    } catch (error) {
      console.error('Failed to stop wake word detection:', error);
    }
  };

  const handleUpdateConfig = async () => {
    try {
      await invoke<string>('update_wake_word_config', {
        sensitivity,
        useLocal,
        useCloud,
        cloudApiKey: null,
        wakeWords: ['jarvis']
      });
      console.log('Wake word configuration updated');
    } catch (error) {
      console.error('Failed to update wake word config:', error);
    }
  };

  return (
    <div className="wake-word-indicator bg-gray-800 rounded-lg p-4 text-white">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold">Wake Word Detection</h3>
        <div className="flex items-center space-x-2">
          <div className={`w-3 h-3 rounded-full ${
            isActive ? 'bg-green-500 animate-pulse' : 'bg-gray-500'
          }`}></div>
          <span className="text-sm">{status}</span>
        </div>
      </div>

      <div className="space-y-4">
        {/* Настройки чувствительности */}
        <div>
          <label className="block text-sm font-medium mb-2">
            Sensitivity: {sensitivity.toFixed(1)}
          </label>
          <input
            type="range"
            min="0"
            max="1"
            step="0.1"
            value={sensitivity}
            onChange={(e) => setSensitivity(parseFloat(e.target.value))}
            className="w-full h-2 bg-gray-700 rounded-lg appearance-none cursor-pointer"
          />
        </div>

        {/* Настройки детекторов */}
        <div className="space-y-2">
          <label className="flex items-center space-x-2">
            <input
              type="checkbox"
              checked={useLocal}
              onChange={(e) => setUseLocal(e.target.checked)}
              className="rounded"
            />
            <span className="text-sm">Use Local Detection</span>
          </label>
          
          <label className="flex items-center space-x-2">
            <input
              type="checkbox"
              checked={useCloud}
              onChange={(e) => setUseCloud(e.target.checked)}
              className="rounded"
            />
            <span className="text-sm">Use Cloud Detection</span>
          </label>
        </div>

        {/* Кнопки управления */}
        <div className="flex space-x-2">
          <button
            onClick={handleStart}
            disabled={isActive}
            className="flex-1 bg-green-600 hover:bg-green-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white py-2 px-4 rounded transition-colors"
          >
            Start
          </button>
          
          <button
            onClick={handleStop}
            disabled={!isActive}
            className="flex-1 bg-red-600 hover:bg-red-700 disabled:bg-gray-600 disabled:cursor-not-allowed text-white py-2 px-4 rounded transition-colors"
          >
            Stop
          </button>
        </div>

        <button
          onClick={handleUpdateConfig}
          className="w-full bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded transition-colors"
        >
          Update Configuration
        </button>
      </div>

      {/* Индикатор активности */}
      <div className="mt-4 p-3 bg-gray-700 rounded">
        <div className="flex items-center justify-between">
          <span className="text-sm">Wake Word Status:</span>
          <div className="flex items-center space-x-2">
            <div className={`w-2 h-2 rounded-full ${
              isActive ? 'bg-green-400' : 'bg-gray-400'
            }`}></div>
            <span className="text-xs">
              {isActive ? 'Listening for "Jarvis"...' : 'Not listening'}
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default WakeWordIndicator;
