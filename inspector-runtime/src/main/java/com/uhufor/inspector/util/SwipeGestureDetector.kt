package com.uhufor.inspector.util

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

internal open class SwipeGestureDetector(
    private val distanceThreshold: Int = SWIPE_DISTANCE_THRESHOLD,
    private val velocityThreshold: Int = SWIPE_VELOCITY_THRESHOLD,
) : GestureDetector.SimpleOnGestureListener() {

    enum class GestureDirection { UP, DOWN, LEFT, RIGHT }

    final override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float,
    ): Boolean {
        if (e1 == null) return false

        val diffX = e2.x - e1.x
        val diffY = e2.y - e1.y

        return if (abs(diffX) > abs(diffY)) {
            if (abs(diffX) > distanceThreshold && abs(velocityX) > velocityThreshold) {
                if (diffX > 0) {
                    onSwipe(GestureDirection.RIGHT)
                } else {
                    onSwipe(GestureDirection.LEFT)
                }
                true
            } else {
                false
            }
        } else {
            if (abs(diffY) > distanceThreshold && abs(velocityY) > velocityThreshold) {
                if (diffY > 0) {
                    onSwipe(GestureDirection.DOWN)
                } else {
                    onSwipe(GestureDirection.UP)
                }
                true
            } else {
                false
            }
        }
    }

    open fun onSwipe(direction: GestureDirection) {}

    companion object {
        private const val SWIPE_DISTANCE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }
}
