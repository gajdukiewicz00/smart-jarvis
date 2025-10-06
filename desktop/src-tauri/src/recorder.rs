mod cpal;

use once_cell::sync::OnceCell;
use std::sync::atomic::{AtomicBool, Ordering};
use crate::config;

static RECORDER_TYPE: OnceCell<config::RecorderType> = OnceCell::new();
static RECORDING: AtomicBool = AtomicBool::new(false);

pub fn init() -> Result<(), ()> {
    if !RECORDER_TYPE.get().is_none() {return Ok(());} // already initialized

    // set default recorder type
    RECORDER_TYPE.set(config::DEFAULT_RECORDER_TYPE).unwrap();

    // load given recorder
    match RECORDER_TYPE.get().unwrap() {
        config::RecorderType::Cpal => {
            // Init CPAL recorder
            log::info!("Initializing CPAL recorder.");
            cpal::init_cpal();
            log::info!("Recorder initialized.");
        }
        config::RecorderType::PvRecorder => {
            log::info!("Initializing PvRecorder.");
            // TODO: Implement PvRecorder support
            log::warn!("PvRecorder not yet implemented");
        }
    }

    Ok(())
}

pub fn start_recording() -> Result<(), String> {
    if RECORDING.load(Ordering::Relaxed) {
        return Err("Already recording".into());
    }

    match RECORDER_TYPE.get().unwrap() {
        config::RecorderType::Cpal => {
            cpal::start_recording()
        }
        config::RecorderType::PvRecorder => {
            Err("PvRecorder not implemented".into())
        }
    }
}

pub fn stop_recording() -> Result<(), String> {
    if !RECORDING.load(Ordering::Relaxed) {
        return Err("Not recording".into());
    }

    match RECORDER_TYPE.get().unwrap() {
        config::RecorderType::Cpal => {
            cpal::stop_recording()
        }
        config::RecorderType::PvRecorder => {
            Err("PvRecorder not implemented".into())
        }
    }
}

pub fn is_recording() -> bool {
    RECORDING.load(Ordering::Relaxed)
}
