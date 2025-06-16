package com.uhufor.inspector.util

import android.util.DisplayMetrics
import com.uhufor.inspector.UnitMode
import kotlin.math.roundToInt

internal object UnitConverter {
    fun format(px: Float, dm: DisplayMetrics, mode: UnitMode): String =
        when (mode) {
            UnitMode.DP -> "${(px / dm.density).roundToInt()}dp"
            UnitMode.PX -> "${px.roundToInt()}px"
        }
}
