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

    private val _traverseType = MutableStateFlow<TraverseType>(TraverseType.DFS)
    val traverseTypeFlow = _traverseType.asStateFlow()
    var traverseType: TraverseType
        get() = _traverseType.value
        set(value) {
            _traverseType.value = value
        }

    private val _enableDetailsView = MutableStateFlow<Boolean>(false)
    val enableDetailsViewFlow = _enableDetailsView.asStateFlow()
    var enableDetailsView: Boolean
        get() = _enableDetailsView.value
        set(value) {
            _enableDetailsView.value = value
        }

    private val _detailsViewUiScale = MutableStateFlow(1.0f)
    val detailsViewUiScaleFlow = _detailsViewUiScale.asStateFlow()
    var detailsViewUiScale: Float
        get() = _detailsViewUiScale.value
        set(value) {
            _detailsViewUiScale.value = value
        }
}
