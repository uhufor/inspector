package com.uhufor.inspector.engine

import android.graphics.RectF
import kotlin.math.abs

object RelativeMeasurement {

    fun calculateDistances(
        primary: RectF,
        secondary: RectF
    ): List<Distance> {
        val distances = mutableListOf<Distance>()
        val horizontalOverlap = !(secondary.right < primary.left || secondary.left > primary.right)
        val verticalOverlap = !(secondary.bottom < primary.top || secondary.top > primary.bottom)

        if (horizontalOverlap && !verticalOverlap) {
            val verticalX = primary.left + primary.width() / 2
            if (secondary.top > primary.bottom) {
                val distance = secondary.top - primary.bottom
                if (distance > 0) {
                    distances.add(
                        Distance(
                            startX = verticalX,
                            startY = primary.bottom,
                            endX = verticalX,
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
                            startX = verticalX,
                            startY = primary.top,
                            endX = verticalX,
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

        } else if (verticalOverlap && !horizontalOverlap) {
            val horizontalY = primary.top + primary.height() / 2
            if (secondary.left > primary.right) {
                val distance = secondary.left - primary.right
                if (distance > 0) {
                    distances.add(
                        Distance(
                            startX = primary.right,
                            startY = horizontalY,
                            endX = secondary.left,
                            endY = horizontalY,
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
                            startY = horizontalY,
                            endX = secondary.right,
                            endY = horizontalY,
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

        } else if (!horizontalOverlap && !verticalOverlap) {
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
