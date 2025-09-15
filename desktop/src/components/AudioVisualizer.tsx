import React, { useState, useEffect, useRef } from 'react';
import { invoke } from '@tauri-apps/api/core';

interface AudioVisualizerProps {
  isActive: boolean;
  audioLevel?: number;
}

interface VisualizationData {
  waveform: number[];
  frequency_spectrum: number[];
  volume_level: number;
  speech_activity: boolean;
  timestamp: number;
}

const AudioVisualizer: React.FC<AudioVisualizerProps> = ({ isActive, audioLevel = 0 }) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [visualizationData, setVisualizationData] = useState<VisualizationData | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    initializeAudioSystem();
  }, []);

  useEffect(() => {
    if (isActive && isInitialized) {
      startVisualization();
    } else {
      stopVisualization();
    }
  }, [isActive, isInitialized]);

  const initializeAudioSystem = async () => {
    try {
      await invoke<string>('initialize_audio_system');
      setIsInitialized(true);
      console.log('Audio system initialized');
    } catch (error) {
      console.error('Failed to initialize audio system:', error);
    }
  };

  const startVisualization = () => {
    // Симуляция аудио данных для демонстрации
    const interval = setInterval(async () => {
      if (!isActive) {
        clearInterval(interval);
        return;
      }

      // Генерируем тестовые аудио данные
      const samples = generateTestAudioSamples();
      
      try {
        const data = await invoke<VisualizationData>('get_audio_visualization', {
          samples
        });
        setVisualizationData(data);
        drawVisualization(data);
      } catch (error) {
        console.error('Failed to get audio visualization:', error);
      }
    }, 100); // Обновляем каждые 100ms
  };

  const stopVisualization = () => {
    setVisualizationData(null);
    clearCanvas();
  };

  const generateTestAudioSamples = (): number[] => {
    const samples = [];
    const frequency = 440; // A4 note
    const sampleRate = 44100;
    const duration = 0.1; // 100ms
    
    for (let i = 0; i < sampleRate * duration; i++) {
      const sample = Math.sin(2 * Math.PI * frequency * i / sampleRate) * audioLevel;
      samples.push(sample);
    }
    
    return samples;
  };

  const drawVisualization = (data: VisualizationData) => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = canvas.width;
    const height = canvas.height;

    // Очищаем canvas
    ctx.clearRect(0, 0, width, height);

    // Рисуем фон
    ctx.fillStyle = 'rgba(0, 0, 0, 0.1)';
    ctx.fillRect(0, 0, width, height);

    // Рисуем форму волны
    drawWaveform(ctx, data.waveform, width, height);

    // Рисуем частотный спектр
    drawFrequencySpectrum(ctx, data.frequency_spectrum, width, height);

    // Рисуем индикатор уровня громкости
    drawVolumeIndicator(ctx, data.volume_level, width, height);

    // Рисуем индикатор активности речи
    drawSpeechActivityIndicator(ctx, data.speech_activity, width, height);
  };

  const drawWaveform = (ctx: CanvasRenderingContext2D, waveform: number[], width: number, height: number) => {
    if (waveform.length === 0) return;

    ctx.strokeStyle = '#00ff00';
    ctx.lineWidth = 2;
    ctx.beginPath();

    const centerY = height / 2;
    const stepX = width / waveform.length;

    for (let i = 0; i < waveform.length; i++) {
      const x = i * stepX;
      const y = centerY - (waveform[i] * centerY * 0.8);
      
      if (i === 0) {
        ctx.moveTo(x, y);
      } else {
        ctx.lineTo(x, y);
      }
    }

    ctx.stroke();
  };

  const drawFrequencySpectrum = (ctx: CanvasRenderingContext2D, spectrum: number[], width: number, height: number) => {
    if (spectrum.length === 0) return;

    const barWidth = width / spectrum.length;
    const maxHeight = height * 0.3;

    for (let i = 0; i < spectrum.length; i++) {
      const barHeight = spectrum[i] * maxHeight;
      const x = i * barWidth;
      const y = height - barHeight;

      // Цвет зависит от частоты
      const hue = (i / spectrum.length) * 360;
      ctx.fillStyle = `hsl(${hue}, 70%, 50%)`;
      ctx.fillRect(x, y, barWidth - 1, barHeight);
    }
  };

  const drawVolumeIndicator = (ctx: CanvasRenderingContext2D, volumeLevel: number, width: number, height: number) => {
    const normalizedVolume = Math.max(0, Math.min(1, (volumeLevel + 60) / 60)); // Конвертируем из дБ
    const indicatorWidth = width * normalizedVolume;
    
    // Цвет зависит от уровня громкости
    let color = '#00ff00'; // Зеленый
    if (normalizedVolume > 0.7) {
      color = '#ff0000'; // Красный
    } else if (normalizedVolume > 0.4) {
      color = '#ffff00'; // Желтый
    }

    ctx.fillStyle = color;
    ctx.fillRect(0, height - 10, indicatorWidth, 10);
  };

  const drawSpeechActivityIndicator = (ctx: CanvasRenderingContext2D, speechActivity: boolean, width: number, height: number) => {
    const radius = 20;
    const x = width - radius - 10;
    const y = radius + 10;

    ctx.fillStyle = speechActivity ? '#ff0000' : '#333333';
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, 2 * Math.PI);
    ctx.fill();

    // Анимация для активности речи
    if (speechActivity) {
      ctx.strokeStyle = '#ff0000';
      ctx.lineWidth = 3;
      ctx.beginPath();
      ctx.arc(x, y, radius + 5, 0, 2 * Math.PI);
      ctx.stroke();
    }
  };

  const clearCanvas = () => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.clearRect(0, 0, canvas.width, canvas.height);
  };

  return (
    <div className="audio-visualizer bg-gray-900 rounded-lg p-4">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-white">Audio Visualizer</h3>
        <div className="flex items-center space-x-2">
          <div className={`w-3 h-3 rounded-full ${
            isActive ? 'bg-green-500 animate-pulse' : 'bg-gray-500'
          }`}></div>
          <span className="text-sm text-gray-300">
            {isActive ? 'Active' : 'Inactive'}
          </span>
        </div>
      </div>

      <div className="relative">
        <canvas
          ref={canvasRef}
          width={400}
          height={200}
          className="w-full h-48 bg-black rounded border border-gray-700"
        />
        
        {/* Информация о визуализации */}
        {visualizationData && (
          <div className="absolute top-2 left-2 text-xs text-gray-300 bg-black bg-opacity-50 p-2 rounded">
            <div>Volume: {visualizationData.volume_level.toFixed(1)} dB</div>
            <div>Speech: {visualizationData.speech_activity ? 'Active' : 'Inactive'}</div>
            <div>Waveform Points: {visualizationData.waveform.length}</div>
            <div>Frequency Bins: {visualizationData.frequency_spectrum.length}</div>
          </div>
        )}
      </div>

      {/* Контролы */}
      <div className="mt-4 flex space-x-2">
        <button
          onClick={initializeAudioSystem}
          className="flex-1 bg-blue-600 hover:bg-blue-700 text-white py-2 px-4 rounded transition-colors"
        >
          Initialize Audio
        </button>
        
        <button
          onClick={() => {
            if (isActive) {
              stopVisualization();
            } else {
              startVisualization();
            }
          }}
          className={`flex-1 py-2 px-4 rounded transition-colors ${
            isActive 
              ? 'bg-red-600 hover:bg-red-700 text-white' 
              : 'bg-green-600 hover:bg-green-700 text-white'
          }`}
        >
          {isActive ? 'Stop' : 'Start'} Visualization
        </button>
      </div>

      {/* Статус */}
      <div className="mt-2 text-xs text-gray-400">
        Status: {isInitialized ? 'Initialized' : 'Not Initialized'} | 
        Audio Level: {(audioLevel * 100).toFixed(1)}%
      </div>
    </div>
  );
};

export default AudioVisualizer;