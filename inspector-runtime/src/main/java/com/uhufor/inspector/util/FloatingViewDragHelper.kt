package com.uhufor.inspector.util

import android.util.Size
import android.view.MotionEvent
import kotlin.math.abs

internal class FloatingViewDragHelper(
    private val screenSizeProvider: ScreenSizeProvider,
    private val delegate: FloatingViewDragHelperDelegate,
    dragTolerance: Int = DEFAULT_DRAG_TOLERANCE,
    horizontalMargin: Int = 0,
) {
    private val dragToleranceDp: Int = dragTolerance.dp().toInt()
    private val horizontalMarginPx: Int = horizontalMargin.dp().toInt()
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f

    var isDragging: Boolean = false
        private set

    init {
        updateScreenDimensions()
    }

    private fun updateScreenDimensions() {
        val screenSize = screenSizeProvider.getSize()
        screenWidth = screenSize.width
        screenHeight = screenSize.height
    }

    fun onDown(event: MotionEvent) {
        updateScreenDimensions()
        getInitialPosition(
            viewPosition = delegate.getPosition(),
            viewSize = delegate.getSize()
        ).let { (x, y) ->
            initialX = x
            initialY = y
        }
        initialTouchX = event.rawX
        initialTouchY = event.rawY
        isDragging = false
    }

    private fun getInitialPosition(viewPosition: Pair<Int, Int>, viewSize: Size): Pair<Int, Int> {
        val (x, y) = viewPosition
        val xMax = (screenWidth - viewSize.width - horizontalMarginPx)
            .coerceAtLeast(horizontalMarginPx)
        val yMax = (screenHeight - viewSize.height).coerceAtLeast(0)

        return x.coerceIn(horizontalMarginPx, xMax) to y.coerceIn(0, yMax)
    }

    fun onMove(event: MotionEvent): Boolean {
        val dX = event.rawX - initialTouchX
        val dY = event.rawY - initialTouchY

        if (!isDragging && (abs(dX) > dragToleranceDp || abs(dY) > dragToleranceDp)) {
            isDragging = true
        }

        if (isDragging) {
            var newX = initialX + dX.toInt()
            var newY = initialY + dY.toInt()

            if (screenWidth == 0 || screenHeight == 0) {
                updateScreenDimensions()
            }

            val viewSize = delegate.getSize()
            newX = newX.coerceIn(
                minimumValue = horizontalMarginPx,
                maximumValue = screenWidth - viewSize.width - horizontalMarginPx
            )
            newY = newY.coerceIn(minimumValue = 0, maximumValue = screenHeight - viewSize.height)

            delegate.onChangePosition(newX, newY)
        }
        return isDragging
    }

    fun onUp(): Boolean {
        return isDragging
    }

    companion object {
        const val DEFAULT_DRAG_TOLERANCE: Int = 4
    }
}
