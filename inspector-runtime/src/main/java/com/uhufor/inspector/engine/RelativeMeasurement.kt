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
        val left = horizontalDistances
            .filter { min(it.startX, it.endX) < primary.right }
            .minByOrNull { it.startX + it.endX }
            .takeIf { primary.left != secondary.left }
        val right = horizontalDistances
            .filter { max(it.startX, it.endX) > primary.left }
            .maxByOrNull { it.startX + it.endX }
            .takeIf { primary.right != secondary.right }
        val verticalDistance = distances.filter { it.type == DistanceType.VERTICAL }
        val top = verticalDistance
            .filter { min(it.startY, it.endY) < primary.bottom }
            .minByOrNull { it.startY + it.endY }
            .takeIf { primary.top != secondary.top }
        val bottom = verticalDistance
            .filter { max(it.startY, it.endY) > primary.top }
            .maxByOrNull { it.startY + it.endY }
            .takeIf { primary.bottom != secondary.bottom }

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
                        type = DistanceType.VERTICAL,
                        primaryEdge = Edge.TOP,
                        secondaryEdge = Edge.TOP,
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
                        type = DistanceType.VERTICAL,
                        primaryEdge = Edge.BOTTOM,
                        secondaryEdge = Edge.BOTTOM,
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
                        type = DistanceType.HORIZONTAL,
                        primaryEdge = Edge.LEFT,
                        secondaryEdge = Edge.LEFT,
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
                        type = DistanceType.HORIZONTAL,
                        primaryEdge = Edge.RIGHT,
                        secondaryEdge = Edge.RIGHT,
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
                            type = DistanceType.VERTICAL,
                            primaryEdge = Edge.BOTTOM,
                            secondaryEdge = Edge.TOP,
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
                            type = DistanceType.VERTICAL,
                            primaryEdge = Edge.TOP,
                            secondaryEdge = Edge.BOTTOM,
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
                        type = DistanceType.HORIZONTAL,
                        primaryEdge = Edge.LEFT,
                        secondaryEdge = Edge.LEFT,
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
                        type = DistanceType.HORIZONTAL,
                        primaryEdge = Edge.RIGHT,
                        secondaryEdge = Edge.RIGHT,
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
                            type = DistanceType.HORIZONTAL,
                            primaryEdge = Edge.RIGHT,
                            secondaryEdge = Edge.LEFT,
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
                            type = DistanceType.HORIZONTAL,
                            primaryEdge = Edge.LEFT,
                            secondaryEdge = Edge.RIGHT,
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
                        type = DistanceType.VERTICAL,
                        primaryEdge = Edge.TOP,
                        secondaryEdge = Edge.TOP,
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
                        type = DistanceType.VERTICAL,
                        primaryEdge = Edge.BOTTOM,
                        secondaryEdge = Edge.BOTTOM,
                    )
                )
            }
        } else {
            val centerY = primary.top + primary.height() / 2f
            val isRight = secondary.left >= primary.right
            val hStartX = if (isRight) primary.right else secondary.right
            val hEndX = if (isRight) secondary.left else primary.left
            val dx = hEndX - hStartX
            if (dx > 0f) {
                distances.add(
                    Distance(
                        startX = hStartX,
                        startY = centerY,
                        endX = hEndX,
                        endY = centerY,
                        value = dx,
                        type = DistanceType.HORIZONTAL,
                        primaryEdge = if (isRight) Edge.RIGHT else Edge.LEFT,
                        secondaryEdge = if (isRight) Edge.LEFT else Edge.RIGHT,
                    )
                )
            }

            val centerX = primary.left + primary.width() / 2f
            val isBelow = secondary.top >= primary.bottom
            val vStartY = if (isBelow) primary.bottom else secondary.bottom
            val vEndY = if (isBelow) secondary.top else primary.top
            val dy = vEndY - vStartY
            if (dy > 0f) {
                distances.add(
                    Distance(
                        startX = centerX,
                        startY = vStartY,
                        endX = centerX,
                        endY = vEndY,
                        value = dy,
                        type = DistanceType.VERTICAL,
                        primaryEdge = if (isBelow) Edge.BOTTOM else Edge.TOP,
                        secondaryEdge = if (isBelow) Edge.TOP else Edge.BOTTOM,
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

enum class Edge {
    LEFT,
    TOP,
    RIGHT,
    BOTTOM,
}

data class Distance(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val value: Float,
    val type: DistanceType,
    val primaryEdge: Edge?,
    val secondaryEdge: Edge?,
)

enum class DistanceType {
    HORIZONTAL,
    VERTICAL,
}
