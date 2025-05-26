package com.uhufor.inspector.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

internal fun checkPermission(context: Context): Boolean {
    if (Settings.canDrawOverlays(context)) return true

    try {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = "package:${context.packageName}".toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (_: Exception) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = "package:${context.packageName}".toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    return false
}
