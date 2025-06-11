package com.uhufor.inspector.engine

import android.graphics.RectF

data class SelectionState(
    val id: Int = 0,
    val bounds: RectF = RectF(),
    val parentBounds: RectF? = null,
    val isClickable: Boolean = false,
)
