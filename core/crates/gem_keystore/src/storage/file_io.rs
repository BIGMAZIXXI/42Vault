use std::fs::{self, File, OpenOptions};
use std::io::{Read, Take};
use std::path::Path;

#[cfg(unix)]
use std::os::unix::fs::{OpenOptionsExt, PermissionsExt};

use crate::KeystoreError;

pub(crate) fn read_capped(path: &Path, cap: usize) -> Result<Vec<u8>, KeystoreError> {
    let mut file = File::open(path)?;
    let mut bytes = Vec::new();
    let mut capped: Take<&mut File> = std::io::Read::by_ref(&mut file).take((cap + 1) as u64);
    capped.read_to_end(&mut bytes)?;
    if bytes.len() > cap {
        return Err(KeystoreError::corrupt_file("file too large"));
    }
    Ok(bytes)
}

pub(super) fn new_secret_file_options() -> OpenOptions {
    let mut options = OpenOptions::new();
    options.write(true).create_new(true);
    #[cfg(unix)]
    set_secret_file_mode(&mut options);
    options
}

#[cfg(unix)]
fn set_secret_file_mode(options: &mut OpenOptions) {
    options.mode(0o600);
}

#[cfg(not(unix))]
fn set_secret_file_mode(_options: &mut OpenOptions) {
    // no-op on non-unix
}

pub(super) fn set_owner_read_write(path: &Path) -> Result<(), KeystoreError> {
    #[cfg(unix)]
    {
        fs::set_permissions(path, fs::Permissions::from_mode(0o600))?;
    }
    #[cfg(not(unix))]
    {
        // ignore on non-unix
    }
    Ok(())
}

pub(super) fn sync_directory(path: &Path) -> Result<(), KeystoreError> {
    #[cfg(unix)]
    {
        File::open(path)?.sync_all()?;
    }
    #[cfg(not(unix))]
    {
        // ignore
    }
    Ok(())
}
