package com.uhufor.inspector.engine

sealed interface MeasurementMode {
    data object Normal : MeasurementMode
    data object Relative : MeasurementMode
}
