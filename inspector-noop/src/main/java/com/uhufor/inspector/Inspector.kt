package com.uhufor.inspector

import android.content.Context

object Inspector {
    var isInspectionEnabled: Boolean = false
        private set

    fun install(context: Context) = Unit
    fun enableInspection() = Unit
    fun disableInspection() = Unit
    fun toggleInspection() = Unit

    fun setUnitMode(mode: UnitMode) = Unit
    fun getUnitMode(): UnitMode = UnitMode.DP

    fun setTraverseType(type: TraverseType) = Unit
    fun getTraverseType(): TraverseType = TraverseType.HIERARCHICAL

    fun enableDetailsView(enabled: Boolean) = Unit
    val isDetailsViewEnabled: Boolean
        get() = false

    fun showFloatingTrigger() = Unit
    fun hideFloatingTrigger() = Unit
}
