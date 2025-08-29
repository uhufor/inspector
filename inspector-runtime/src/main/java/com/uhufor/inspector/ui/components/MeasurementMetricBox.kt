package com.uhufor.inspector.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.uhufor.inspector.ui.compose.dvdp

@Composable
internal fun MeasurementMetricBox(
    leftContent: @Composable BoxScope.() -> Unit,
    topContent: @Composable BoxScope.() -> Unit,
    rightContent: @Composable BoxScope.() -> Unit,
    bottomContent: @Composable BoxScope.() -> Unit,
    centerContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Gray,
    strokeWidth: Dp = 1.dvdp,
    segmentLength: Dp = 12.dvdp,
    minCellHeight: Dp = 24.dvdp,
) {
    val density = LocalDensity.current
    val strokePx = with(density) { strokeWidth.toPx() }
    val segPx = with(density) { segmentLength.toPx() }
    val halfSeg = segPx / 2f

    fun DrawScope.drawCenterV(color: Color, stroke: Float) {
        val cx = size.width / 2f
        drawLine(color, Offset(cx, 0f), Offset(cx, size.height), strokeWidth = stroke)
    }

    fun DrawScope.drawCenterH(color: Color, stroke: Float) {
        val cy = size.height / 2f
        drawLine(color, Offset(0f, cy), Offset(size.width, cy), strokeWidth = stroke)
    }

    fun DrawScope.drawTopSegment(color: Color, stroke: Float, halfSeg: Float) {
        val cx = size.width / 2f
        val x1 = (cx - halfSeg).coerceIn(0f, size.width)
        val x2 = (cx + halfSeg).coerceIn(0f, size.width)
        drawLine(color, Offset(x1, 0f), Offset(x2, 0f), strokeWidth = stroke)
    }

    fun DrawScope.drawBottomSegment(color: Color, stroke: Float, halfSeg: Float) {
        val cx = size.width / 2f
        val x1 = (cx - halfSeg).coerceIn(0f, size.width)
        val x2 = (cx + halfSeg).coerceIn(0f, size.width)
        drawLine(color, Offset(x1, size.height), Offset(x2, size.height), strokeWidth = stroke)
    }

    fun DrawScope.drawLeftSegment(color: Color, stroke: Float, halfSeg: Float) {
        val cy = size.height / 2f
        val y1 = (cy - halfSeg).coerceIn(0f, size.height)
        val y2 = (cy + halfSeg).coerceIn(0f, size.height)
        drawLine(color, Offset(0f, y1), Offset(0f, y2), strokeWidth = stroke)
    }

    fun DrawScope.drawRightSegment(color: Color, stroke: Float, halfSeg: Float) {
        val cy = size.height / 2f
        val y1 = (cy - halfSeg).coerceIn(0f, size.height)
        val y2 = (cy + halfSeg).coerceIn(0f, size.height)
        drawLine(color, Offset(size.width, y1), Offset(size.width, y2), strokeWidth = stroke)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = minCellHeight)
                    .drawWithCache {
                        onDrawBehind {
                            drawCenterV(lineColor, strokePx)
                            drawTopSegment(lineColor, strokePx, halfSeg)
                        }
                    }
            ) { topContent() }

            Spacer(modifier = Modifier.weight(1f))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .heightIn(min = minCellHeight)
                    .drawWithCache {
                        onDrawBehind {
                            drawCenterH(lineColor, strokePx)
                            drawLeftSegment(lineColor, strokePx, halfSeg)
                        }
                    }
            ) { leftContent() }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = minCellHeight)
                    .drawWithCache {
                        onDrawBehind {
                            drawTopSegment(lineColor, strokePx, halfSeg)
                            drawRightSegment(lineColor, strokePx, halfSeg)
                            drawBottomSegment(lineColor, strokePx, halfSeg)
                            drawLeftSegment(lineColor, strokePx, halfSeg)
                        }
                    }
            ) { centerContent() }

            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .heightIn(min = minCellHeight)
                    .drawWithCache {
                        onDrawBehind {
                            drawCenterH(lineColor, strokePx)
                            drawRightSegment(lineColor, strokePx, halfSeg)
                        }
                    }
            ) { rightContent() }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = minCellHeight)
                    .drawWithCache {
                        onDrawBehind {
                            drawCenterV(lineColor, strokePx)
                            drawBottomSegment(lineColor, strokePx, halfSeg)
                        }
                    }
            ) { bottomContent() }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
