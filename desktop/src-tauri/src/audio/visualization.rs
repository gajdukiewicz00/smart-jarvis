use super::AudioData;
use std::collections::VecDeque;

/// Аудио визуализатор
pub struct AudioVisualizer {
    fft_buffer: VecDeque<f32>,
    max_buffer_size: usize,
    frequency_bins: usize,
}

impl AudioVisualizer {
    pub fn new(max_buffer_size: usize, frequency_bins: usize) -> Self {
        Self {
            fft_buffer: VecDeque::new(),
            max_buffer_size,
            frequency_bins,
        }
    }

    /// Обновить визуализацию с новыми аудио данными
    pub fn update(&mut self, audio_data: &AudioData) -> VisualizationData {
        // Добавляем новые сэмплы в буфер
        for sample in &audio_data.samples {
            self.fft_buffer.push_back(*sample);
        }

        // Ограничиваем размер буфера
        while self.fft_buffer.len() > self.max_buffer_size {
            self.fft_buffer.pop_front();
        }

        // Вычисляем данные для визуализации
        let waveform = self.calculate_waveform();
        let frequency_spectrum = self.calculate_frequency_spectrum();
        let volume_level = self.calculate_volume_level();
        let speech_activity = self.detect_speech_activity();

        VisualizationData {
            waveform,
            frequency_spectrum,
            volume_level,
            speech_activity,
            timestamp: chrono::Utc::now().timestamp_millis() as u64,
        }
    }

    /// Вычислить форму волны
    fn calculate_waveform(&self) -> Vec<f32> {
        let samples: Vec<f32> = self.fft_buffer.iter().cloned().collect();
        
        if samples.is_empty() {
            return Vec::new();
        }

        // Упрощаем форму волны для визуализации
        let target_points = 100; // Количество точек для отображения
        let chunk_size = samples.len() / target_points.max(1);
        
        let mut waveform = Vec::new();
        for chunk in samples.chunks(chunk_size.max(1)) {
            if !chunk.is_empty() {
                let avg = chunk.iter().sum::<f32>() / chunk.len() as f32;
                waveform.push(avg);
            }
        }

        waveform
    }

    /// Вычислить частотный спектр
    fn calculate_frequency_spectrum(&self) -> Vec<f32> {
        let samples: Vec<f32> = self.fft_buffer.iter().cloned().collect();
        
        if samples.len() < 2 {
            return vec![0.0; self.frequency_bins];
        }

        // Простое вычисление спектра мощности (без FFT)
        let mut spectrum = vec![0.0; self.frequency_bins];
        
        // Разделяем аудио на частотные полосы
        let samples_per_bin = samples.len() / self.frequency_bins;
        
        for (i, bin) in spectrum.iter_mut().enumerate() {
            let start = i * samples_per_bin;
            let end = ((i + 1) * samples_per_bin).min(samples.len());
            
            if start < end {
                let bin_samples = &samples[start..end];
                let energy = bin_samples.iter().map(|&x| x * x).sum::<f32>() / bin_samples.len() as f32;
                *bin = energy.sqrt();
            }
        }

        spectrum
    }

    /// Вычислить уровень громкости
    fn calculate_volume_level(&self) -> f32 {
        let samples: Vec<f32> = self.fft_buffer.iter().cloned().collect();
        
        if samples.is_empty() {
            return 0.0;
        }

        // RMS (Root Mean Square) для уровня громкости
        let rms = (samples.iter().map(|&x| x * x).sum::<f32>() / samples.len() as f32).sqrt();
        
        // Конвертируем в децибелы
        if rms > 0.0 {
            20.0 * rms.log10()
        } else {
            -60.0 // Минимальный уровень
        }
    }

    /// Обнаружить активность речи
    fn detect_speech_activity(&self) -> bool {
        let samples: Vec<f32> = self.fft_buffer.iter().cloned().collect();
        
        if samples.is_empty() {
            return false;
        }

        // Простое обнаружение активности речи
        let energy = samples.iter().map(|&x| x * x).sum::<f32>();
        let threshold = 0.01; // Порог энергии

        energy > threshold
    }

    /// Получить данные для спектрограммы
    pub fn get_spectrogram_data(&self, window_size: usize) -> Vec<Vec<f32>> {
        let samples: Vec<f32> = self.fft_buffer.iter().cloned().collect();
        
        if samples.len() < window_size {
            return Vec::new();
        }

        let mut spectrogram = Vec::new();
        
        // Скользящее окно по аудио
        for window in samples.windows(window_size) {
            let spectrum = self.calculate_window_spectrum(window);
            spectrogram.push(spectrum);
        }

        spectrogram
    }

    /// Вычислить спектр для окна
    fn calculate_window_spectrum(&self, window: &[f32]) -> Vec<f32> {
        let mut spectrum = vec![0.0; self.frequency_bins];
        
        // Простое вычисление спектра для окна
        let samples_per_bin = window.len() / self.frequency_bins;
        
        for (i, bin) in spectrum.iter_mut().enumerate() {
            let start = i * samples_per_bin;
            let end = ((i + 1) * samples_per_bin).min(window.len());
            
            if start < end {
                let bin_samples = &window[start..end];
                let energy = bin_samples.iter().map(|&x| x * x).sum::<f32>() / bin_samples.len() as f32;
                *bin = energy.sqrt();
            }
        }

        spectrum
    }

    /// Получить данные для осциллографа
    pub fn get_oscilloscope_data(&self, max_points: usize) -> Vec<f32> {
        let samples: Vec<f32> = self.fft_buffer.iter().cloned().collect();
        
        if samples.len() <= max_points {
            return samples;
        }

        // Прореживаем сэмплы для отображения
        let step = samples.len() / max_points;
        samples.iter().step_by(step).cloned().take(max_points).collect()
    }
}

/// Данные для визуализации
#[derive(Debug, Clone, serde::Serialize, serde::Deserialize)]
pub struct VisualizationData {
    pub waveform: Vec<f32>,
    pub frequency_spectrum: Vec<f32>,
    pub volume_level: f32,
    pub speech_activity: bool,
    pub timestamp: u64,
}

impl VisualizationData {
    /// Получить нормализованный уровень громкости (0.0 - 1.0)
    pub fn get_normalized_volume(&self) -> f32 {
        // Конвертируем из дБ в нормализованное значение
        let min_db = -60.0;
        let max_db = 0.0;
        
        if self.volume_level <= min_db {
            0.0
        } else if self.volume_level >= max_db {
            1.0
        } else {
            (self.volume_level - min_db) / (max_db - min_db)
        }
    }

    /// Получить цвет для визуализации на основе уровня громкости
    pub fn get_volume_color(&self) -> (u8, u8, u8) {
        let normalized = self.get_normalized_volume();
        
        if normalized < 0.3 {
            // Зеленый для низкого уровня
            (0, 255, 0)
        } else if normalized < 0.7 {
            // Желтый для среднего уровня
            (255, 255, 0)
        } else {
            // Красный для высокого уровня
            (255, 0, 0)
        }
    }

    /// Получить данные для гистограммы частот
    pub fn get_frequency_bars(&self, bar_count: usize) -> Vec<f32> {
        if self.frequency_spectrum.is_empty() {
            return vec![0.0; bar_count];
        }

        let spectrum = &self.frequency_spectrum;
        let bars_per_bin = spectrum.len() / bar_count.max(1);
        
        let mut bars = Vec::new();
        for i in 0..bar_count {
            let start = i * bars_per_bin;
            let end = ((i + 1) * bars_per_bin).min(spectrum.len());
            
            if start < end {
                let avg = spectrum[start..end].iter().sum::<f32>() / (end - start) as f32;
                bars.push(avg);
            } else {
                bars.push(0.0);
            }
        }

        bars
    }
}
