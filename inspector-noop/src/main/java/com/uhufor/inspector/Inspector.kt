package com.uhufor.inspector

import android.content.Context

object Inspector {
    var isInspectionEnabled: Boolean = false
        private set

    private var unitMode: UnitMode = UnitMode.DP
    private var traverseType: TraverseType = TraverseType.HIERARCHICAL
    private var showDetailsView: Boolean = false

    fun install(context: Context) = Unit
    fun enableInspection() { isInspectionEnabled = true }
    fun disableInspection() { isInspectionEnabled = false }
    fun toggleInspection() { isInspectionEnabled = !isInspectionEnabled }

    fun setUnitMode(mode: UnitMode) { unitMode = mode }
    fun getUnitMode(): UnitMode = unitMode

    fun setTraverseType(type: TraverseType) { traverseType = type }
    fun getTraverseType(): TraverseType = traverseType

    fun enableDetailsView(enabled: Boolean) { showDetailsView = enabled }
    val isDetailsViewEnabled: Boolean
        get() = showDetailsView

    fun refresh() = Unit

    fun showFloatingTrigger() = Unit
    fun hideFloatingTrigger() = Unit

    fun setDetailsViewFocusable(focusable: Boolean) = Unit
}
