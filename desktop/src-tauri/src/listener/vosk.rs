use crate::{config, stt};

pub fn init() -> Result<(), ()> {
    // Vosk уже инициализирован в STT модуле
    Ok(())
}

pub fn data_callback(frame_buffer: &[i16]) -> Option<i32> {
    // Используем STT для распознавания wake word фразы
    if let Some(text) = stt::recognize(frame_buffer, true) {
        let text_lower = text.to_lowercase();
        if text_lower.contains(config::VOSK_FETCH_PHRASE) {
            log::info!("Wake word detected via Vosk: {}", text);
            return Some(1);
        }
    }
    None
}
