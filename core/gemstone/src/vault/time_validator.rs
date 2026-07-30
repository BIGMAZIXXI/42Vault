use super::lock_manager::VaultError;
use std::time::{SystemTime, UNIX_EPOCH};

pub struct TimeValidator;

impl TimeValidator {
    pub fn new() -> Self {
        Self
    }

    pub fn validate(&self, ntp_time: u64, system_time: u64) -> bool {
        let diff = if ntp_time > system_time {
            ntp_time - system_time
        } else {
            system_time - ntp_time
        };

        diff < 300
    }

    pub fn get_system_time() -> Result<u64, VaultError> {
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .map_err(|_| VaultError::TimeError)
            .map(|d| d.as_secs())
    }
}

impl Default for TimeValidator {
    fn default() -> Self {
        Self::new()
    }
}