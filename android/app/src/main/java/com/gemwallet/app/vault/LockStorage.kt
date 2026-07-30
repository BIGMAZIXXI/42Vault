package com.gemwallet.app.vault

import android.content.Context
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest

data class AssetLock(
    val assetId: String,
    val amount: String,
    val unlockTimestamp: Long,
    val lockTimestamp: Long,
    val networkType: String // "EVM", "UTXO", "SOLANA", "OTHER"
)

class LockStorage(private val context: Context) {
    
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val prefs = EncryptedSharedPreferences.create(
        "vault_locks_secure",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val gson = Gson()
    private val lockType = object : TypeToken<List<AssetLock>>() {}.type
    
    private val _locks = MutableStateFlow<List<AssetLock>>(emptyList())
    val locks: StateFlow<List<AssetLock>> = _locks.asStateFlow()
    
    init {
        loadLocks()
    }
    
    fun saveLocks(locks: List<AssetLock>) {
        val json = gson.toJson(locks)
        prefs.edit().putString("locks", json).apply()
        _locks.value = locks
    }
    
    private fun loadLocks() {
        val json = prefs.getString("locks", "[]") ?: "[]"
        val locks: List<AssetLock> = gson.fromJson(json, lockType)
        _locks.value = locks
    }
    
    fun getDeviceFingerprint(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val salt = "42Vault_SALT_2024"
        val combined = "$androidId:$salt"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(combined.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}