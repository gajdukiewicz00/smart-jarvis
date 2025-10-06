mod rustpotter;
mod vosk;

use once_cell::sync::OnceCell;
use std::sync::atomic::{AtomicBool, Ordering};
use crate::{config, stt};
use crate::config::WakeWordEngine;

static WAKE_WORD_ENGINE: OnceCell<WakeWordEngine> = OnceCell::new();
static LISTENING: AtomicBool = AtomicBool::new(false);

pub fn init() -> Result<(), ()> {
    if !WAKE_WORD_ENGINE.get().is_none() {return Ok(());} // already initialized

    // store current engine - для начала используем Rustpotter как наиболее надежный
    WAKE_WORD_ENGINE.set(WakeWordEngine::Rustpotter).unwrap();

    // load given wake-word engine
    match WAKE_WORD_ENGINE.get().unwrap() {
        WakeWordEngine::Rustpotter => {
            // Init Rustpotter wake-word engine
            log::info!("Initializing Rustpotter wake-word engine.");
            return rustpotter::init();
        },
        WakeWordEngine::Porcupine => {
            // Init Porcupine wake-word engine - временно не реализован
            log::info!("Porcupine wake-word engine not yet implemented.");
            return Err(());
        },
        WakeWordEngine::Vosk => {
            // Init Vosk as wake-word engine (very slow, though)
            log::info!("Initializing Vosk as wake-word engine.");
            log::warn!("Using Vosk as wake-word engine is highly not recommended, because it's very slow for this task.");
            return vosk::init();
        },
    }
}

pub fn data_callback(frame_buffer: &[i16]) -> Option<i32> {
    match WAKE_WORD_ENGINE.get().unwrap() {
        WakeWordEngine::Rustpotter => {
            rustpotter::data_callback(frame_buffer)
        },
        WakeWordEngine::Porcupine => {
            // Porcupine не реализован
            log::debug!("Porcupine wake word engine not implemented");
            None
        },
        WakeWordEngine::Vosk => {
            vosk::data_callback(frame_buffer)
        }
    }
}

pub fn is_listening() -> bool {
    LISTENING.load(Ordering::Relaxed)
}

pub fn set_listening(listening: bool) {
    LISTENING.store(listening, Ordering::Relaxed);
}
