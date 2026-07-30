use serde::{Deserialize, Serialize};
use std::collections::HashMap;
use thiserror::Error;

use super::ntp_client::NtpClient;
use super::time_validator::TimeValidator;

#[derive(Error, Debug)]
pub enum VaultError {
    #[error("Время на устройстве расходится с NTP более чем на 5 минут")]
    TimeManipulationDetected,
    #[error("NTP-сервер недоступен")]
    NtpUnavailable,
    #[error("Актив уже заблокирован")]
    AssetAlreadyLocked,
    #[error("Актив не найден")]
    AssetNotFound,
    #[error("Блокировка ещё активна")]
    LockStillActive,
    #[error("Не удалось получить время")]
    TimeError,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AssetLock {
    pub asset_id: String,
    pub amount: String,
    pub unlock_timestamp: u64,
    pub lock_timestamp: u64,
    pub network_type: NetworkType,
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub enum NetworkType {
    Evm,
    Utxo,
    Solana,
    Other,
}

pub struct LockManager {
    locks: HashMap<String, AssetLock>,
    ntp_client: NtpClient,
    time_validator: TimeValidator,
}

impl LockManager {
    pub fn new() -> Self {
        Self {
            locks: HashMap::new(),
            ntp_client: NtpClient::new(),
            time_validator: TimeValidator::new(),
        }
    }

    pub fn create_lock(
        &mut self,
        asset_id: String,
        amount: String,
        duration_seconds: u64,
        network_type: NetworkType,
    ) -> Result<AssetLock, VaultError> {
        if self.locks.contains_key(&asset_id) {
            return Err(VaultError::AssetAlreadyLocked);
        }

        let ntp_time = self.ntp_client.get_verified_time()?;
        let system_time = std::time::SystemTime::now()
            .duration_since(std::time::UNIX_EPOCH)
            .map_err(|_| VaultError::TimeError)?
            .as_secs();

        if !self.time_validator.validate(ntp_time, system_time) {
            return Err(VaultError::TimeManipulationDetected);
        }

        let lock = AssetLock {
            asset_id: asset_id.clone(),
            amount,
            unlock_timestamp: ntp_time + duration_seconds,
            lock_timestamp: ntp_time,
            network_type,
        };

        self.locks.insert(asset_id, lock.clone());
        Ok(lock)
    }

    pub fn can_unlock(&self, asset_id: &str) -> Result<bool, VaultError> {
        let lock = self.locks.get(asset_id).ok_or(VaultError::AssetNotFound)?;
        let ntp_time = self.ntp_client.get_verified_time()?;
        Ok(ntp_time >= lock.unlock_timestamp)
    }

    pub fn get_all_locks(&self) -> Vec<&AssetLock> {
        self.locks.values().collect()
    }

    pub fn get_lock(&self, asset_id: &str) -> Option<&AssetLock> {
        self.locks.get(asset_id)
    }

    pub fn remove_lock(&mut self, asset_id: &str) -> Option<AssetLock> {
        self.locks.remove(asset_id)
    }
}

impl Default for LockManager {
    fn default() -> Self {
        Self::new()
    }
}