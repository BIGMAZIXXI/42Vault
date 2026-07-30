package com.gemwallet.app.vault

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun VaultScreen(
    viewModel: VaultViewModel = hiltViewModel()
) {
    val locks by viewModel.locks.collectAsState()
    val availableAssets by viewModel.availableAssets.collectAsState()
    val ntpStatus by viewModel.ntpStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🔒 42Vault",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Финансовый дисциплинатор",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        NtpStatusCard(status = ntpStatus)

        Spacer(modifier = Modifier.height(16.dp))

        if (locks.isNotEmpty()) {
            Text(
                text = "Активные блокировки (${locks.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(locks) { lock ->
                    LockItem(
                        lock = lock,
                        onUnlock = { viewModel.checkAndUnlock(lock) }
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Text(
                    text = "Нет активных блокировок",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Доступные для блокировки",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.height(250.dp)
        ) {
            items(availableAssets) { asset ->
                LockAssetItem(
                    asset = asset,
                    onLockClick = { duration ->
                        viewModel.createLock(asset, duration)
                    }
                )
            }
        }
    }
}

@Composable
fun LockAssetItem(
    asset: AvailableAsset,
    onLockClick: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${asset.symbol} (${asset.networkType})",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Баланс: ${asset.balanceFormatted}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                ReliabilityIndicator(networkType = asset.networkType)
            }

            var expanded by remember { mutableStateOf(false) }

            Box {
                Button(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    Text("🔒 Заблокировать")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf(
                        30L * 86400 to "1 месяц",
                        90L * 86400 to "3 месяца",
                        180L * 86400 to "6 месяцев",
                        365L * 86400 to "1 год"
                    ).forEach { (seconds, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onLockClick(seconds)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReliabilityIndicator(networkType: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (networkType) {
            "EVM" -> {
                Text(
                    text = "✅ Абсолютная",
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            "UTXO" -> {
                Text(
                    text = "⚠️ Локальная",
                    color = Color(0xFFFF9800),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            else -> {
                Text(
                    text = "ℹ️ Частичная",
                    color = Color(0xFF2196F3),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun LockItem(
    lock: AssetLock,
    onUnlock: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = lock.assetId.uppercase(),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Сумма: ${lock.amount}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Разблокировка: ${formatTimestamp(lock.unlockTimestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = "Тип: ${lock.networkType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            val now = System.currentTimeMillis() / 1000
            if (now >= lock.unlockTimestamp) {
                Button(
                    onClick = onUnlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("🔓 Разблокировать")
                }
            } else {
                val remaining = lock.unlockTimestamp - now
                Text(
                    text = "⏳ ${formatDuration(remaining)}",
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun NtpStatusCard(status: NtpStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (status) {
                is NtpStatus.Synced -> Color(0xFFE8F5E9)
                is NtpStatus.Checking -> Color(0xFFFFF3E0)
                is NtpStatus.Error -> Color(0xFFFFEBEE)
                is NtpStatus.TimeManipulationDetected -> Color(0xFFFFEBEE)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (status) {
                is NtpStatus.Synced -> {
                    Text("✅ Время синхронизировано")
                }
                is NtpStatus.Checking -> {
                    Text("🔄 Проверка времени...")
                }
                is NtpStatus.Error -> {
                    Text("❌ Ошибка: ${status.message}")
                }
                is NtpStatus.TimeManipulationDetected -> {
                    Text("🚨 Обнаружена манипуляция временем!")
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp * 1000)
    val format = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return format.format(date)
}

private fun formatDuration(seconds: Long): String {
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    return if (days > 0) "$days д $hours ч" else "$hours ч"
}