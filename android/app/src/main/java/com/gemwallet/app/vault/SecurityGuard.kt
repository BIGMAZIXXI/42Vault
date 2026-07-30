package com.gemwallet.app.vault

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

class SecurityGuard(private val context: Context) {
    
    fun checkSecurity(): SecurityStatus {
        return when {
            isRooted() -> SecurityStatus.RootDetected
            isEmulator() -> SecurityStatus.EmulatorDetected
            isDebuggerAttached() -> SecurityStatus.DebuggerAttached
            else -> SecurityStatus.Secure
        }
    }
    
    private fun isRooted(): Boolean {
        val suPaths = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        
        for (path in suPaths) {
            if (File(path).exists()) return true
        }
        
        val buildTags = android.os.Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }
        
        return false
    }
    
    private fun isEmulator(): Boolean {
        val build = android.os.Build
        return build.FINGERPRINT.startsWith("generic") ||
               build.FINGERPRINT.startsWith("unknown") ||
               build.MODEL.contains("google_sdk") ||
               build.MODEL.contains("Emulator") ||
               build.MODEL.contains("Android SDK built for x86") ||
               build.MANUFACTURER.contains("Genymotion") ||
               (build.BRAND.startsWith("generic") && build.DEVICE.startsWith("generic"))
    }
    
    private fun isDebuggerAttached(): Boolean {
        return android.os.Debug.isDebuggerConnected()
    }
}

enum class SecurityStatus {
    Secure,
    RootDetected,
    EmulatorDetected,
    DebuggerAttached
}