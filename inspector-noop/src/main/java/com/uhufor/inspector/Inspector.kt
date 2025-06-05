package com.uhufor.inspector

import android.content.Context

object Inspector {
    var isInspectionEnabled: Boolean = false
        private set

    val isDfsTraverseEnabled: Boolean
        get() = false

    fun install(context: Context) = Unit
    fun enableInspection() = Unit
    fun disableInspection() = Unit
    fun toggleInspection() = Unit

    fun setUnitMode(mode: UnitMode) = Unit
    fun getUnitMode(): UnitMode = UnitMode.DP

    fun enableDfsTraverse() = Unit
    fun disableDfsTraverse() = Unit

    fun showFloatingTrigger() = Unit
    fun hideFloatingTrigger() = Unit
}
