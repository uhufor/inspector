package com.uhufor.inspector.config

import com.uhufor.inspector.TraverseType
import com.uhufor.inspector.UnitMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class Config {
    private val _unitModeFlow = MutableStateFlow<UnitMode>(UnitMode.DP)
    val unitModeFlow = _unitModeFlow.asStateFlow()
    var unitMode: UnitMode
        get() = _unitModeFlow.value
        set(value) {
            _unitModeFlow.value = value
        }

    private val _traverseType = MutableStateFlow<TraverseType>(TraverseType.HIERARCHICAL)
    val traverseTypeFlow = _traverseType.asStateFlow()
    var traverseType: TraverseType
        get() = _traverseType.value
        set(value) {
            _traverseType.value = value
        }

    private val _showDetailsView = MutableStateFlow<Boolean>(false)
    val showDetailsViewFlow = _showDetailsView.asStateFlow()
    var showDetailsView: Boolean
        get() = _showDetailsView.value
        set(value) {
            _showDetailsView.value = value
        }
}
