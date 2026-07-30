use std::net::UdpSocket;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

use super::lock_manager::VaultError;

pub struct NtpClient {
    servers: Vec<String>,
}

impl NtpClient {
    pub fn new() -> Self {
        Self {
            servers: vec![
                "time.google.com".to_string(),
                "pool.ntp.org".to_string(),
                "time.windows.com".to_string(),
                "0.pool.ntp.org".to_string(),
            ],
        }
    }

    pub fn get_verified_time(&self) -> Result<u64, VaultError> {
        let mut times = Vec::new();

        for server in &self.servers {
            if let Ok(time) = self.query_server(server) {
                times.push(time);
            }
        }

        if times.len() < 2 {
            return Err(VaultError::NtpUnavailable);
        }

        times.sort();
        let median = times[times.len() / 2];

        for &time in &times {
            if (time as i64 - median as i64).abs() > 10 {
                return Err(VaultError::TimeManipulationDetected);
            }
        }

        Ok(median)
    }

    fn query_server(&self, server: &str) -> Result<u64, VaultError> {
        let socket = UdpSocket::bind("0.0.0.0:0").map_err(|_| VaultError::NtpUnavailable)?;
        socket
            .set_read_timeout(Some(Duration::from_secs(5)))
            .map_err(|_| VaultError::NtpUnavailable)?;

        let mut packet = [0u8; 48];
        packet[0] = 0b00100011;

        socket
            .send_to(&packet, (server, 123))
            .map_err(|_| VaultError::NtpUnavailable)?;

        socket
            .recv_from(&mut packet)
            .map_err(|_| VaultError::NtpUnavailable)?;

        let mut timestamp: u64 = 0;
        for i in 40..44 {
            timestamp = (timestamp << 8) | packet[i] as u64;
        }

        let ntp_epoch = 2_208_988_800;
        let time = timestamp.saturating_sub(ntp_epoch);

        if time == 0 {
            return Err(VaultError::NtpUnavailable);
        }

        Ok(time)
    }
}

impl Default for NtpClient {
    fn default() -> Self {
        Self::new()
    }
}