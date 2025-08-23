package com.uhufor.inspector.util

import android.graphics.Rect

internal interface AnchorView {
    interface OnPositionRectChangeListener {
        fun onPositionRectChange(rect: Rect)
    }
}
