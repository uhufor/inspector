package com.uhufor.inspector.engine

internal sealed interface MeasurementMode {
    data object Normal : MeasurementMode
    data object Relative : MeasurementMode
}
