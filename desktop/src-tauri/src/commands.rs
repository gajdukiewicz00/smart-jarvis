use rand::seq::SliceRandom;
use seqdiff::ratio;
use serde::{Deserialize, Serialize};
use std::path::Path;
use std::{fs, fs::File, process::Command};
use core::time::Duration;
use std::path::PathBuf;

mod structs;
pub use structs::*;

use crate::{config, audio};

pub fn parse_commands() -> Result<Vec<AssistantCommand>, String> {
    // collect commands
    let mut commands: Vec<AssistantCommand> = vec![];

    // read commands directories first
    let commands_path = std::env::current_dir().unwrap().join(config::COMMANDS_PATH);
    if let Ok(cpaths) = fs::read_dir(commands_path) {
        for cpath in cpaths {
            // validate this command, check if required files exists
            let _cpath = cpath.unwrap().path();
            let cc_file = Path::new(&_cpath).join("command.yaml");

            if cc_file.exists() {
                // try parse config files
                let cc_reader = std::fs::File::open(&cc_file).unwrap();
                let cc_yaml: CommandsList;

                // try parse command.yaml
                match serde_yaml::from_reader::<File, CommandsList>(cc_reader) {
                    Ok(parse_result) => {
                        cc_yaml = parse_result;
                    },
                    Err(msg) => {
                        log::warn!("Can't parse {}, skipping ...\nCommand parse error is: {:?}", &cc_file.display(), msg);
                        continue;
                    }
                }
                // everything seems to be Ok
                commands.push(AssistantCommand {
                    path: _cpath,
                    commands: cc_yaml,
                });
            }
        }

        if commands.len() > 0 {
            Ok(commands)
        } else {
            log::error!("No commands were found");
            Err("No commands were found".into())
        }
    } else {
        log::error!("Error reading commands directory");
        return Err("Error reading commands directory".into());
    }
}

pub fn fetch_command<'a>(
    phrase: &str,
    commands: &'a Vec<AssistantCommand>,
) -> Option<(&'a PathBuf, &'a Config)> {
    // result scmd
    let mut result_scmd: Option<(&PathBuf, &Config)> = None;
    let mut current_max_ratio = config::CMD_RATIO_THRESHOLD;

    // convert fetch phrase to sequence
    let fetch_phrase_chars = phrase.chars().collect::<Vec<_>>();

    // list all the commands
    for cmd in commands {
        // list all subcommands
        for scmd in &cmd.commands.list {
            // list all phrases in command
            for cmd_phrase in &scmd.phrases {
                // convert cmd phrase to sequence
                let cmd_phrase_chars = cmd_phrase.chars().collect::<Vec<_>>();

                // compare fetch phrase with cmd phrase
                let ratio = ratio(&fetch_phrase_chars, &cmd_phrase_chars);

                // return, if it fits the given threshold
                if ratio >= current_max_ratio {
                    result_scmd = Some((&cmd.path, &scmd));
                    current_max_ratio = ratio;
                }
            }
        }
    }

    if let Some((cmd_path, scmd)) = result_scmd {
        log::info!("CMD is: {:?}, SCMD is: {:?}, Ratio is: {}", cmd_path, scmd, current_max_ratio);
        Some((cmd_path, scmd))
    } else {
        None
    }
}

pub fn execute_exe(exe: &str, args: &Vec<String>) -> std::io::Result<std::process::Child> {
    Command::new(exe).args(args).spawn()
}

pub fn execute_cli(cmd: &str, args: &Vec<String>) -> std::io::Result<std::process::Child> {
    if cfg!(target_os = "windows") {
        Command::new("cmd")
                .arg("/C")
                .arg(cmd)
                .args(args)
                .spawn()
    } else {
        Command::new("sh")
                .arg("-c")
                .arg(cmd)
                .args(args)
                .spawn()
    }
}

pub fn execute_command(
    cmd_path: &PathBuf,
    cmd_config: &Config,
) -> Result<bool, String> {
    match cmd_config.command.action.as_str() {
        "voice" => {
            // VOICE command type
            let random_cmd_sound = format!("{}.wav", cmd_config.voice.sounds.choose(&mut rand::thread_rng()).unwrap());
            // audio::play_sound(&sounds_directory.join(random_cmd_sound));

            Ok(true)
        }
        "exe" => {
            // EXE command type
            let exe_path_absolute = Path::new(&cmd_config.command.exe_path);
            let exe_path_local = Path::new(&cmd_path).join(&cmd_config.command.exe_path);

            if let Ok(_) = execute_exe(
                if exe_path_absolute.exists() {
                    exe_path_absolute.to_str().unwrap()
                } else {
                    exe_path_local.to_str().unwrap()
                },
                &cmd_config.command.exe_args,
            ) {
                let random_cmd_sound = format!("{}.wav", cmd_config.voice.sounds.choose(&mut rand::thread_rng()).unwrap());
                // audio::play_sound(&sounds_directory.join(random_cmd_sound));

                Ok(true)
            } else {
                log::error!("EXE process spawn error (does exe path is valid?)");
                Err("EXE process spawn error (does exe path is valid?)".into())
            }
        }
        "cli" => {
            // CLI command type
            let cli_cmd = &cmd_config.command.cli_cmd;

            match execute_cli(
                cli_cmd,
                &cmd_config.command.cli_args,
            ) {
                    Ok(_) => {
                        let random_cmd_sound = format!("{}.wav", cmd_config.voice.sounds.choose(&mut rand::thread_rng()).unwrap());
                        // audio::play_sound(&sounds_directory.join(random_cmd_sound));

                    Ok(true)
                },
                Err(msg) => {
                    log::error!("CLI command error ({})", msg);
                    Err(format!("Shell command error ({})", msg).into())
                }
            }
        }
        "terminate" => {
            // TERMINATE command type
            std::thread::sleep(Duration::from_secs(2));
            std::process::exit(0);
        }
        "stop_chaining" => {
            // STOP_CHAINING command type
            Ok(false)
        }
        _ => {
            log::error!("Command type unknown");
            Err("Command type unknown".into())
        },
    }
}

pub fn list(from: &[AssistantCommand]) -> Vec<String> {
    let mut out: Vec<String> = vec![];

    for x in from.iter() {
        out.push(String::from(x.path.to_str().unwrap()));
    }

    out
}
