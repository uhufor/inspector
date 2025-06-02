package com.uhufor.inspector

import android.app.Application

object Inspector {
    var isInspectionEnabled: Boolean = false
        private set

    fun install(app: Application) = Unit
    fun enableInspection() {
        isInspectionEnabled = true
    }

    fun disableInspection() {
        isInspectionEnabled = false
    }

    fun toggleInspection() {
        isInspectionEnabled = !isInspectionEnabled
    }

    fun showFloatingTrigger() = Unit
    fun hideFloatingTrigger() = Unit
}
