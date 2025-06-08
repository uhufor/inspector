package com.uhufor.inspector.util

import android.util.Size

internal fun interface ScreenSizeProvider {
    fun getSize(): Size
}

internal interface FloatingViewDragHelperDelegate {
    fun getPosition(): Pair<Int, Int>
    fun getSize(): Size
    fun onChangePosition(x: Int, y: Int)
}
