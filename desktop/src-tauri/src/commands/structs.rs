use serde::{Deserialize, Serialize};
use std::path::PathBuf;

#[derive(Debug, Serialize, Deserialize)]
pub struct AssistantCommand {
    pub path: PathBuf,
    pub commands: CommandsList,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct CommandsList {
    pub list: Vec<Config>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct Config {
    pub command: ConfigCommandSection,
    pub voice: ConfigVoiceSection,
    pub phrases: Vec<String>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ConfigCommandSection {
    pub action: String,
    #[serde(default)]
    pub exe_path: String,
    #[serde(default)]
    pub exe_args: Vec<String>,
    #[serde(default)]
    pub cli_cmd: String,
    #[serde(default)]
    pub cli_args: Vec<String>
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ConfigVoiceSection {
    #[serde(default)]
    pub sounds: Vec<String>,
}
