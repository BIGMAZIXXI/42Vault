package com.gemwallet.android.ui.navigation.routes

import androidx.navigation3.runtime.NavEntryBuilder
import com.gemwallet.android.ui.navigation.NavKey

const val vaultRoute = "vault"

fun NavEntryBuilder<NavKey>.vaultScreen(
    onCancel: () -> Unit,
): Unit = entry(vaultRoute) {
    // TODO: Подключить VaultScreen
    // VaultScreen()
}