use super::AudioData;
use std::collections::VecDeque;

/// Аудио процессор
pub struct AudioProcessor {
    buffer: VecDeque<f32>,
    max_buffer_size: usize,
}

impl AudioProcessor {
    pub fn new(max_buffer_size: usize) -> Self {
        Self {
            buffer: VecDeque::new(),
            max_buffer_size,
        }
    }

    /// Обработать аудио данные
    pub fn process(&mut self, audio_data: &mut AudioData) -> AudioData {
        // Добавляем новые сэмплы в буфер
        for sample in &audio_data.samples {
            self.buffer.push_back(*sample);
        }

        // Ограничиваем размер буфера
        while self.buffer.len() > self.max_buffer_size {
            self.buffer.pop_front();
        }

        // Применяем обработку
        let processed_samples = self.apply_processing();

        AudioData::new(
            processed_samples,
            audio_data.sample_rate,
            audio_data.channels,
        )
    }

    /// Применить обработку к аудио
    fn apply_processing(&self) -> Vec<f32> {
        let samples: Vec<f32> = self.buffer.iter().cloned().collect();
        
        // Применяем различные эффекты
        let normalized = self.normalize(&samples);
        let filtered = self.apply_low_pass_filter(&normalized);
        let enhanced = self.enhance_speech(&filtered);
        
        enhanced
    }

    /// Нормализация аудио
    fn normalize(&self, samples: &[f32]) -> Vec<f32> {
        if samples.is_empty() {
            return Vec::new();
        }

        // Находим максимальное значение
        let max_val = samples.iter()
            .map(|&x| x.abs())
            .fold(0.0_f32, |a, b| a.max(b));

        if max_val == 0.0 {
            return samples.to_vec();
        }

        // Нормализуем к 0.8 (оставляем запас)
        let factor = 0.8 / max_val;
        samples.iter().map(|&x| x * factor).collect()
    }

    /// Применить низкочастотный фильтр
    fn apply_low_pass_filter(&self, samples: &[f32]) -> Vec<f32> {
        if samples.len() < 2 {
            return samples.to_vec();
        }

        let mut filtered = Vec::with_capacity(samples.len());
        let alpha = 0.1; // Коэффициент фильтра

        filtered.push(samples[0]);
        for i in 1..samples.len() {
            let filtered_sample = alpha * samples[i] + (1.0 - alpha) * filtered[i - 1];
            filtered.push(filtered_sample);
        }

        filtered
    }

    /// Улучшение речи
    fn enhance_speech(&self, samples: &[f32]) -> Vec<f32> {
        // Простое улучшение речи - усиление средних частот
        samples.iter().map(|&sample| {
            // Простая обработка для улучшения речи
            sample * 1.2 // Усиление
        }).collect()
    }

    /// Обнаружение тишины
    pub fn detect_silence(&self, samples: &[f32], threshold: f32) -> bool {
        if samples.is_empty() {
            return true;
        }

        let rms = (samples.iter().map(|&x| x * x).sum::<f32>() / samples.len() as f32).sqrt();
        rms < threshold
    }

    /// Обнаружение активности речи
    pub fn detect_speech_activity(&self, samples: &[f32]) -> bool {
        if samples.is_empty() {
            return false;
        }

        // Простое обнаружение активности речи
        let energy = samples.iter().map(|&x| x * x).sum::<f32>();
        let threshold = 0.01; // Порог энергии

        energy > threshold
    }

    /// Получить уровень громкости
    pub fn get_volume_level(&self, samples: &[f32]) -> f32 {
        if samples.is_empty() {
            return 0.0;
        }

        let rms = (samples.iter().map(|&x| x * x).sum::<f32>() / samples.len() as f32).sqrt();
        rms
    }

    /// Обрезать тишину в начале и конце
    pub fn trim_silence(&self, mut audio_data: AudioData, silence_threshold: f32) -> AudioData {
        let samples = &mut audio_data.samples;
        
        // Находим начало речи
        let mut start = 0;
        for (i, sample) in samples.iter().enumerate() {
            if sample.abs() > silence_threshold {
                start = i;
                break;
            }
        }

        // Находим конец речи
        let mut end = samples.len();
        for (i, sample) in samples.iter().enumerate().rev() {
            if sample.abs() > silence_threshold {
                end = i + 1;
                break;
            }
        }

        // Обрезаем аудио
        if start < end {
            audio_data.samples = samples[start..end].to_vec();
        } else {
            audio_data.samples.clear();
        }

        audio_data
    }

    /// Разделить аудио на чанки
    pub fn split_into_chunks(&self, audio_data: &AudioData, chunk_size_ms: u64) -> Vec<AudioData> {
        let samples_per_chunk = (audio_data.sample_rate as u64 * chunk_size_ms / 1000) as usize;
        let mut chunks = Vec::new();

        for chunk in audio_data.samples.chunks(samples_per_chunk) {
            if !chunk.is_empty() {
                chunks.push(AudioData::new(
                    chunk.to_vec(),
                    audio_data.sample_rate,
                    audio_data.channels,
                ));
            }
        }

        chunks
    }

    /// Объединить аудио чанки
    pub fn merge_chunks(&self, chunks: &[AudioData]) -> AudioData {
        if chunks.is_empty() {
            return AudioData::new(Vec::new(), 44100, 1);
        }

        let sample_rate = chunks[0].sample_rate;
        let channels = chunks[0].channels;
        let mut all_samples = Vec::new();

        for chunk in chunks {
            all_samples.extend_from_slice(&chunk.samples);
        }

        AudioData::new(all_samples, sample_rate, channels)
    }
}
