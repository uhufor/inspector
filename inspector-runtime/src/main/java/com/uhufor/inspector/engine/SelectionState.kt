package com.uhufor.inspector.engine

import android.graphics.RectF

data class SelectionState(
    val bounds: RectF,
    val isClickable: Boolean = false,
    val parentBounds: RectF? = null,
    val id: Int = 0,
)
