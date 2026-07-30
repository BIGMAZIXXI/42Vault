package com.gemwallet.app.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AvailableAsset(
    val id: String,
    val symbol: String,
    val balance: Double,
    val balanceFormatted: String,
    val networkType: String
)

sealed class NtpStatus {
    object Synced : NtpStatus()
    object Checking : NtpStatus()
    data class Error(val message: String) : NtpStatus()
    object TimeManipulationDetected : NtpStatus()
}

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val lockStorage: LockStorage
) : ViewModel() {

    private val _locks = MutableStateFlow(lockStorage.locks.value)
    val locks: StateFlow<List<AssetLock>> = _locks.asStateFlow()

    private val _ntpStatus = MutableStateFlow<NtpStatus>(NtpStatus.Checking)
    val ntpStatus: StateFlow<NtpStatus> = _ntpStatus.asStateFlow()

    private val _availableAssets = MutableStateFlow<List<AvailableAsset>>(emptyList())
    val availableAssets: StateFlow<List<AvailableAsset>> = _availableAssets.asStateFlow()

    init {
        viewModelScope.launch {
            loadAvailableAssets()
            // TODO: Проверка времени через NTP
            _ntpStatus.value = NtpStatus.Synced
        }
    }

    fun createLock(asset: AvailableAsset, durationSeconds: Long) {
        viewModelScope.launch {
            try {
                val lock = AssetLock(
                    assetId = asset.id,
                    amount = asset.balance.toString(),
                    unlockTimestamp = System.currentTimeMillis() / 1000 + durationSeconds,
                    lockTimestamp = System.currentTimeMillis() / 1000,
                    networkType = asset.networkType
                )

                val currentLocks = _locks.value.toMutableList()
                currentLocks.add(lock)
                _locks.value = currentLocks
                lockStorage.saveLocks(currentLocks)

                loadAvailableAssets()
            } catch (e: Exception) {
                // Обработка ошибки
            }
        }
    }

    fun checkAndUnlock(lock: AssetLock) {
        viewModelScope.launch {
            val now = System.currentTimeMillis() / 1000
            if (now >= lock.unlockTimestamp) {
                val currentLocks = _locks.value.toMutableList()
                currentLocks.remove(lock)
                _locks.value = currentLocks
                lockStorage.saveLocks(currentLocks)
                loadAvailableAssets()
            }
        }
    }

    private suspend fun loadAvailableAssets() {
        // TODO: Получить активы из кошелька
        // Пока заглушка
        val lockedIds = _locks.value.map { it.assetId }.toSet()
        val dummyAssets = listOf(
            AvailableAsset("eth", "ETH", 1.5, "1.5 ETH", "EVM"),
            AvailableAsset("btc", "BTC", 0.05, "0.05 BTC", "UTXO"),
            AvailableAsset("sol", "SOL", 10.0, "10.0 SOL", "SOLANA")
        )
        _availableAssets.value = dummyAssets.filter { !lockedIds.contains(it.id) }
    }
}