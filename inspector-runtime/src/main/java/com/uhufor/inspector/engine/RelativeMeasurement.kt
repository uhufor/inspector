package com.uhufor.inspector.engine

import android.graphics.RectF
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

internal object RelativeMeasurement {

    fun getRelativeBounds(
        primary: RectF,
        secondary: RectF,
    ): RectF {
        val distances = calculateDistances(primary, secondary)
        val horizontalDistances = distances.filter { it.type == DistanceType.HORIZONTAL }
        val left = horizontalDistances.find { min(it.startX, it.endX) < primary.left }
        val right = horizontalDistances.find { max(it.startX, it.endX) > primary.right }
        val verticalDistance = distances.filter { it.type == DistanceType.VERTICAL }
        val top = verticalDistance.find { min(it.startY, it.endY) < primary.top }
        val bottom = verticalDistance.find { max(it.startY, it.endY) > primary.bottom }

        return RectF(
            left?.value ?: 0F,
            top?.value ?: 0F,
            right?.value ?: 0F,
            bottom?.value ?: 0F
        )
    }

    fun calculateDistances(
        primary: RectF,
        secondary: RectF,
    ): List<Distance> {
        val distances = mutableListOf<Distance>()
        val horizontalOverlap =
            !(secondary.right <= primary.left || secondary.left >= primary.right)
        val verticalOverlap =
            !(secondary.bottom <= primary.top || secondary.top >= primary.bottom)

        if (horizontalOverlap && verticalOverlap) {
            val overlapCenterX = getHorizontalOverlapCenterX(primary, secondary)
            val overlapCenterY = getVerticalOverlapCenterY(primary, secondary)

            val topDistanceVal = abs(primary.top - secondary.top)
            if (topDistanceVal > 0f) {
                distances.add(
                    Distance(
                        startX = overlapCenterX,
                        startY = min(primary.top, secondary.top),
                        endX = overlapCenterX,
                        endY = max(primary.top, secondary.top),
                        value = topDistanceVal,
                        type = DistanceType.VERTICAL
                    )
                )
            }

            val bottomDistanceVal = abs(primary.bottom - secondary.bottom)
            if (bottomDistanceVal > 0f) {
                distances.add(
                    Distance(
                        startX = overlapCenterX,
                        startY = min(primary.bottom, secondary.bottom),
                        endX = overlapCenterX,
                        endY = max(primary.bottom, secondary.bottom),
                        value = bottomDistanceVal,
                        type = DistanceType.VERTICAL
                    )
                )
            }

            val leftDistanceVal = abs(primary.left - secondary.left)
            if (leftDistanceVal > 0f) {
                distances.add(
                    Distance(
                        startX = min(primary.left, secondary.left),
                        startY = overlapCenterY,
                        endX = max(primary.left, secondary.left),
                        endY = overlapCenterY,
                        value = leftDistanceVal,
                        type = DistanceType.HORIZONTAL
                    )
                )
            }

            val rightDistanceVal = abs(primary.right - secondary.right)
            if (rightDistanceVal > 0f) {
                distances.add(
                    Distance(
                        startX = min(primary.right, secondary.right),
                        startY = overlapCenterY,
                        endX = max(primary.right, secondary.right),
                        endY = overlapCenterY,
                        value = rightDistanceVal,
                        type = DistanceType.HORIZONTAL
                    )
                )
            }
        } else if (horizontalOverlap) {
            val centerX = getHorizontalOverlapCenterX(primary, secondary)
            if (secondary.top > primary.bottom) {
                val distance = secondary.top - primary.bottom
                if (distance > 0) {
                    distances.add(
                        Distance(
                            startX = centerX,
                            startY = primary.bottom,
                            endX = centerX,
                            endY = secondary.top,
                            value = distance,
                            type = DistanceType.VERTICAL
                        )
                    )
                }
            } else {
                val distance = primary.top - secondary.bottom
                if (distance > 0) {
                    distances.add(
                        Distance(
                            startX = centerX,
                            startY = primary.top,
                            endX = centerX,
                            endY = secondary.bottom,
                            value = distance,
                            type = DistanceType.VERTICAL
                        )
                    )
                }
            }

            val leftDistance = abs(primary.left - secondary.left)
            if (leftDistance > 0) {
                val startX = if (primary.left < secondary.left) primary.left else secondary.left
                val endX = if (primary.left < secondary.left) secondary.left else primary.left
                val y = if (secondary.top > primary.bottom) {
                    (primary.bottom + secondary.top) / 2
                } else {
                    (secondary.bottom + primary.top) / 2
                }

                distances.add(
                    Distance(
                        startX = startX,
                        startY = y,
                        endX = endX,
                        endY = y,
                        value = leftDistance,
                        type = DistanceType.HORIZONTAL
                    )
                )
            }

            val rightDistance = abs(primary.right - secondary.right)
            if (rightDistance > 0) {
                val startX = if (primary.right < secondary.right) primary.right else secondary.right
                val endX = if (primary.right < secondary.right) secondary.right else primary.right
                val y = if (secondary.top > primary.bottom) {
                    (primary.bottom + secondary.top) / 2
                } else {
                    (secondary.bottom + primary.top) / 2
                }

                distances.add(
                    Distance(
                        startX = startX,
                        startY = y,
                        endX = endX,
                        endY = y,
                        value = rightDistance,
                        type = DistanceType.HORIZONTAL
                    )
                )
            }

        } else if (verticalOverlap) {
            val centerY = getVerticalOverlapCenterY(primary, secondary)
            if (secondary.left > primary.right) {
                val distance = secondary.left - primary.right
                if (distance > 0) {
                    distances.add(
                        Distance(
                            startX = primary.right,
                            startY = centerY,
                            endX = secondary.left,
                            endY = centerY,
                            value = distance,
                            type = DistanceType.HORIZONTAL
                        )
                    )
                }
            } else {
                val distance = primary.left - secondary.right
                if (distance > 0) {
                    distances.add(
                        Distance(
                            startX = primary.left,
                            startY = centerY,
                            endX = secondary.right,
                            endY = centerY,
                            value = distance,
                            type = DistanceType.HORIZONTAL
                        )
                    )
                }
            }

            val topDistance = abs(primary.top - secondary.top)
            if (topDistance > 0) {
                val startY = if (primary.top < secondary.top) primary.top else secondary.top
                val endY = if (primary.top < secondary.top) secondary.top else primary.top
                val x = if (secondary.left > primary.right) {
                    (primary.right + secondary.left) / 2
                } else {
                    (secondary.right + primary.left) / 2
                }

                distances.add(
                    Distance(
                        startX = x,
                        startY = startY,
                        endX = x,
                        endY = endY,
                        value = topDistance,
                        type = DistanceType.VERTICAL
                    )
                )
            }

            val bottomDistance = abs(primary.bottom - secondary.bottom)
            if (bottomDistance > 0) {
                val startY =
                    if (primary.bottom < secondary.bottom) primary.bottom else secondary.bottom
                val endY =
                    if (primary.bottom < secondary.bottom) secondary.bottom else primary.bottom
                val x = if (secondary.left > primary.right) {
                    (primary.right + secondary.left) / 2
                } else {
                    (secondary.right + primary.left) / 2
                }

                distances.add(
                    Distance(
                        startX = x,
                        startY = startY,
                        endX = x,
                        endY = endY,
                        value = bottomDistance,
                        type = DistanceType.VERTICAL
                    )
                )
            }
        } else {
            val horizontalY = primary.top + primary.height() / 2
            if (secondary.left > primary.right) {
                distances.add(
                    Distance(
                        startX = primary.right,
                        startY = horizontalY,
                        endX = secondary.left,
                        endY = horizontalY,
                        value = secondary.left - primary.right,
                        type = DistanceType.HORIZONTAL
                    )
                )
            } else {
                distances.add(
                    Distance(
                        startX = primary.left,
                        startY = horizontalY,
                        endX = secondary.right,
                        endY = horizontalY,
                        value = primary.left - secondary.right,
                        type = DistanceType.HORIZONTAL
                    )
                )
            }

            val verticalX = primary.left + primary.width() / 2
            if (secondary.top > primary.bottom) {
                distances.add(
                    Distance(
                        startX = verticalX,
                        startY = primary.bottom,
                        endX = verticalX,
                        endY = secondary.top,
                        value = secondary.top - primary.bottom,
                        type = DistanceType.VERTICAL
                    )
                )
            } else {
                distances.add(
                    Distance(
                        startX = verticalX,
                        startY = primary.top,
                        endX = verticalX,
                        endY = secondary.bottom,
                        value = primary.top - secondary.bottom,
                        type = DistanceType.VERTICAL
                    )
                )
            }
        }
        return distances
    }

    private fun getHorizontalOverlapCenterX(rect1: RectF, rect2: RectF): Float {
        return getHorizontalOverlap(rect1, rect2)
            ?.run { first + (second - first) / 2 }
            ?: (if (rect1.width() > rect2.width()) getCenterX(rect2) else getCenterX(rect1))
    }

    private fun getHorizontalOverlap(rect1: RectF, rect2: RectF): Pair<Float, Float>? {
        val startX = max(rect1.left, rect2.left)
        val endX = min(rect1.right, rect2.right)

        return if (startX < endX) Pair(startX, endX) else null
    }

    private fun getCenterX(rect: RectF): Float {
        return rect.left + rect.width() / 2
    }

    private fun getVerticalOverlapCenterY(rect1: RectF, rect2: RectF): Float {
        return getVerticalOverlap(rect1, rect2)
            ?.run { first + (second - first) / 2 }
            ?: (if (rect1.height() > rect2.height()) getCenterY(rect2) else getCenterY(rect1))
    }

    private fun getVerticalOverlap(rect1: RectF, rect2: RectF): Pair<Float, Float>? {
        val startY = max(rect1.top, rect2.top)
        val endY = min(rect1.bottom, rect2.bottom)

        return if (startY < endY) Pair(startY, endY) else null
    }

    private fun getCenterY(rect: RectF): Float {
        return rect.top + rect.height() / 2
    }
}

data class Distance(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val value: Float,
    val type: DistanceType,
)

enum class DistanceType {
    HORIZONTAL,
    VERTICAL,
}
