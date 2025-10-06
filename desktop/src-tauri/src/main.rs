// Prevents additional console window on Windows in release, DO NOT REMOVE!!
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

fn main() {
  // On Linux force using XDG portal for media permissions (microphone prompt)
  #[cfg(target_os = "linux")]
  {
    std::env::set_var("GTK_USE_PORTAL", "1");
  }
  smartjarvis_desktop_lib::run();
}
