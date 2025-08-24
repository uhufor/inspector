@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.uhufor.inspector.ui

import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uhufor.inspector.UnitMode
import com.uhufor.inspector.engine.SelectionState
import com.uhufor.inspector.engine.UiNodeActionProperties
import com.uhufor.inspector.engine.UiNodeProperties
import com.uhufor.inspector.engine.UiNodeStyleProperties
import com.uhufor.inspector.engine.UiNodeType
import kotlin.math.roundToInt

@Composable
internal fun ElementDetails(
    selectionState: SelectionState,
    unitMode: UnitMode,
    isEditMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onRequestFocusable: (Boolean) -> Unit,
    onApplyMarginPadding: (Int, Int, Int, Int, Int, Int, Int, Int) -> Unit,
) {
    val density = LocalDensity.current.density
    val editMode = isEditMode

    val size = remember(selectionState.properties.size, unitMode, density) {
        formatSizeString(selectionState.properties.size, unitMode, density)
    }

    Card(
        modifier = Modifier
            .width(148.dp)
            .height(180.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .verticalScroll(state = rememberScrollState())
        ) {
            if (!editMode) {
                SectionTitle("Details")
                InfoRow(
                    "ID (${selectionState.properties.type.value})",
                    selectionState.properties.id
                )
                InfoRow("Size", size)

                Measurement(selectionState, unitMode)
                MarginPadding(
                    selectionState,
                    unitMode,
                    onEditRequest = {
                        onRequestFocusable(true)
                        onEditModeChange(true)
                    }
                )
                Actions(selectionState)
                Styles(selectionState)
            } else {
                EditMarginPadding(
                    initialMargin = selectionState.properties.margin,
                    initialPadding = selectionState.properties.padding,
                    unitMode = unitMode,
                    onCancel = {
                        onRequestFocusable(false)
                        onEditModeChange(false)
                    },
                    onApply = { ml, mt, mr, mb, pl, pt, pr, pb ->
                        onApplyMarginPadding(ml, mt, mr, mb, pl, pt, pr, pb)
                        onRequestFocusable(false)
                        onEditModeChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 2.dp),
        textDecoration = TextDecoration.Underline,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .padding(vertical = 1.dp),
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .background(
                    Color(0xCCC0C0C0),
                    shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp)
                )
                .padding(horizontal = 2.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = value,
            fontSize = 9.sp,
            modifier = Modifier
                .fillMaxHeight()
                .weight(weight = 1f, fill = true)
                .background(
                    Color(0xCCFEFEFE),
                    shape = RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
                .padding(horizontal = 2.dp)
                .basicMarquee()
        )
    }
}

@Composable
private fun Measurement(
    selectionState: SelectionState,
    unitMode: UnitMode,
) {
    val density = LocalDensity.current.density
    val size = remember(selectionState.properties.size, unitMode, density) {
        formatSizeString(selectionState.properties.size, unitMode, density)
    }

    val margin = remember(selectionState.properties.distance, unitMode, density) {
        selectionState.properties.distance.formatAsList(unitMode, density)
    }

    Spacer(modifier = Modifier.height(8.dp))
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Distance")
        Spacer(modifier = Modifier.height(2.dp))

        MeasurementMetricBox(
            {
                Text(
                    text = margin[0],
                    fontSize = 8.sp,
                    modifier = Modifier
                        .background(Color(0xFFFFFFCC))
                        .padding(1.dp)
                        .align(Alignment.Center)
                )
            },
            {
                Text(
                    text = margin[1],
                    fontSize = 8.sp,
                    modifier = Modifier
                        .background(Color(0xFFFFFFCC))
                        .padding(1.dp)
                        .align(Alignment.Center)
                )
            },
            {
                Text(
                    text = margin[2],
                    fontSize = 8.sp,
                    modifier = Modifier
                        .background(Color(0xFFFFFFCC))
                        .padding(1.dp)
                        .align(Alignment.Center)
                )
            },
            {
                Text(
                    text = margin[3],
                    fontSize = 8.sp,
                    modifier = Modifier
                        .background(Color(0xFFFFFFCC))
                        .padding(1.dp)
                        .align(Alignment.Center)
                )
            },
            {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                ) {
                    Text(
                        text = size,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .background(Color(0xFFCCFFCC))
                            .padding(2.dp)
                            .align(Alignment.Center)
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = Color(0xCCFEFEFE),
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(4.dp)
        )
    }
}

@Composable
private fun MeasurementMetricBox(
    leftContent: @Composable BoxScope.() -> Unit,
    topContent: @Composable BoxScope.() -> Unit,
    rightContent: @Composable BoxScope.() -> Unit,
    bottomContent: @Composable BoxScope.() -> Unit,
    centerContent: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Gray,
    strokeWidth: Dp = 1.dp,
    segmentLength: Dp = 12.dp,
    minCellHeight: Dp = 24.dp,
) {
    val density = LocalDensity.current
    val strokePx = remember(density, strokeWidth) { with(density) { strokeWidth.toPx() } }
    val segPx = remember(density, segmentLength) { with(density) { segmentLength.toPx() } }
    val halfSeg = remember(segPx) { segPx / 2f }

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

@Composable
private fun MarginPadding(
    selectionState: SelectionState,
    unitMode: UnitMode,
    onEditRequest: () -> Unit,
) {
    val density = LocalDensity.current.density
    val isView = selectionState.properties.type == UiNodeType.VIEW

    val margin = remember(selectionState.properties.margin, unitMode, density) {
        selectionState.properties.margin.formatAsList(unitMode, density)
    }

    val padding = remember(selectionState.properties.padding, unitMode, density) {
        selectionState.properties.padding.formatAsList(unitMode, density)
    }

    Spacer(modifier = Modifier.height(8.dp))
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            SectionTitle("Margin / Padding")
            Spacer(modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(color = Color(0xFFDDEEDD), shape = RoundedCornerShape(4.dp))
                    .clickable(enabled = isView, onClick = onEditRequest)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(text = "Edit", fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .background(Color(0xFFFFFFCC))
                .fillMaxWidth()
                .border(1.dp, Color.Gray)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(margin[1], fontSize = 8.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        margin[0],
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(24.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .weight(weight = 1.0f, fill = true)
                            .height(48.dp)
                            .padding(horizontal = 2.dp, vertical = 4.dp)
                            .border(1.dp, Color.Gray)
                            .background(Color(0xFFCCFFCC))
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                    ) {
                        Text(
                            padding[1],
                            fontSize = 8.sp,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                        Text(
                            padding[0],
                            fontSize = 8.sp,
                            modifier = Modifier.align(Alignment.CenterStart)
                        )
                        Text(
                            padding[2],
                            fontSize = 8.sp,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                        Text(
                            padding[3],
                            fontSize = 8.sp,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        margin[2],
                        fontSize = 8.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(24.dp)
                    )
                }
                Text(margin[3], fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun Actions(selectionState: SelectionState) {
    if (selectionState.properties.actions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        SectionTitle("Actions")
        selectionState.properties.actions.forEach { action ->
            InfoRow(action.value, "true")
        }
    }
}

@Composable
private fun Styles(selectionState: SelectionState) {
    if (selectionState.properties.styles.isNotEmpty()) {
        val (density, fontScale) = LocalDensity.current.run {
            density to fontScale
        }
        Spacer(modifier = Modifier.height(8.dp))
        SectionTitle("Styles")
        selectionState.properties.styles.forEach { style ->
            when (style) {
                is UiNodeStyleProperties.ColorStyle -> {
                    style.backgroundType?.let { backgroundType ->
                        InfoRow(
                            "Bg Type",
                            backgroundType
                        )
                    }
                    style.backgroundColor?.let { backgroundColor ->
                        InfoRow(
                            "Bg Color",
                            "#${backgroundColor.toHexString()}"
                        )
                    }
                }

                is UiNodeStyleProperties.TextStyle -> {
                    InfoRow(
                        "Text",
                        style.text
                    )
                    style.textColor?.let { textColor ->
                        InfoRow(
                            "Text Color",
                            "#${textColor.toHexString()}"
                        )
                    }
                    style.textSize?.let { textSize ->
                        val sizeInDp = textSize / density
                        val sizeInSp = sizeInDp / fontScale
                        InfoRow(
                            "Text Size",
                            "${sizeInSp.roundToInt()}sp, ${sizeInDp.roundToInt()}dp, ${textSize.roundToInt()}px"
                        )
                    }
                    if (style.isBold || style.isItalic) {
                        val typefaceStyles = buildList {
                            if (style.isBold) {
                                add("Bold")
                            }
                            if (style.isItalic) {
                                add("Italic")
                            }
                        }
                        InfoRow(
                            "Text Style",
                            typefaceStyles.joinToString(", ")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditMarginPadding(
    initialMargin: RectF,
    initialPadding: RectF,
    unitMode: UnitMode,
    onCancel: () -> Unit,
    onApply: (Int, Int, Int, Int, Int, Int, Int, Int) -> Unit,
) {
    val density = LocalDensity.current.density
    fun format(v: Float): String = when (unitMode) {
        UnitMode.DP -> ((v / density).roundToInt()).toString()
        UnitMode.PX -> (v.roundToInt()).toString()
    }

    var ml by remember(
        initialMargin,
        unitMode,
        density
    ) { mutableStateOf(format(initialMargin.left)) }
    var mt by remember(
        initialMargin,
        unitMode,
        density
    ) { mutableStateOf(format(initialMargin.top)) }
    var mr by remember(
        initialMargin,
        unitMode,
        density
    ) { mutableStateOf(format(initialMargin.right)) }
    var mb by remember(
        initialMargin,
        unitMode,
        density
    ) { mutableStateOf(format(initialMargin.bottom)) }

    var pl by remember(
        initialPadding,
        unitMode,
        density
    ) { mutableStateOf(format(initialPadding.left)) }
    var pt by remember(
        initialPadding,
        unitMode,
        density
    ) { mutableStateOf(format(initialPadding.top)) }
    var pr by remember(
        initialPadding,
        unitMode,
        density
    ) { mutableStateOf(format(initialPadding.right)) }
    var pb by remember(
        initialPadding,
        unitMode,
        density
    ) { mutableStateOf(format(initialPadding.bottom)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        SectionTitle("Edit Margin/Padding")
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier
                    .height(8.dp)
                    .weight(0.6f)
            )
            Text(
                text = "Margin",
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Padding",
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // left
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Text(
                text = "Left",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.6f)
            )
            SmallNumberField(ml, { ml = it }, modifier = Modifier.weight(1f))
            SmallNumberField(pl, { pl = it }, modifier = Modifier.weight(1f))
        }
        // top
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Text(
                text = "Top",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.6f)
            )
            SmallNumberField(mt, { mt = it }, modifier = Modifier.weight(1f))
            SmallNumberField(pt, { pt = it }, modifier = Modifier.weight(1f))
        }
        // right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Text(
                text = "Right",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.6f)
            )
            SmallNumberField(mr, { mr = it }, modifier = Modifier.weight(1f))
            SmallNumberField(pr, { pr = it }, modifier = Modifier.weight(1f))
        }
        // bottom
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
        ) {
            Text(
                text = "Bottom",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.6f)
            )
            SmallNumberField(mb, { mb = it }, modifier = Modifier.weight(1f))
            SmallNumberField(pb, { pb = it }, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            SmallButton(
                text = "Cancel",
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            SmallButton(
                text = "Apply",
                onClick = {
                    fun parse(s: String): Float = s.toFloatOrNull() ?: 0f
                    val f = if (unitMode == UnitMode.DP) density else 1f
                    val mlPx = (parse(ml) * f).roundToInt()
                    val mtPx = (parse(mt) * f).roundToInt()
                    val mrPx = (parse(mr) * f).roundToInt()
                    val mbPx = (parse(mb) * f).roundToInt()
                    val plPx = (parse(pl) * f).roundToInt()
                    val ptPx = (parse(pt) * f).roundToInt()
                    val prPx = (parse(pr) * f).roundToInt()
                    val pbPx = (parse(pb) * f).roundToInt()
                    onApply(mlPx, mtPx, mrPx, mbPx, plPx, ptPx, prPx, pbPx)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
private fun SmallNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(36.dp)
            .height(20.dp)
            .padding(vertical = 0.5.dp, horizontal = 2.dp)
            .background(Color(0xFFEFEFEF), shape = RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color(0xFF111111),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun SmallButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = Color(0xFFDDEEDD), shape = RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

private fun Int.toHexString(): String = Integer.toHexString(this).uppercase().padStart(8, '0')

@Preview(showBackground = true)
@Composable
internal fun ElementDetailPreview() {
    var isEditMode by remember { mutableStateOf(false) }
    val selectionState = SelectionState(
        id = 0,
        bounds = RectF(0f, 0f, 200f, 100f),
        parentBounds = RectF(0f, 0f, 400f, 200f),
        properties = UiNodeProperties(
            type = UiNodeType.VIEW,
            id = "app:id/showDetail",
            size = android.util.Size(200, 100),
            distance = RectF(11f, 10f, 5f, 5f),
            margin = RectF(16f, 16f, 16f, 16f),
            padding = RectF(2f, 8f, 2f, 10f),
            actions = setOf(
                UiNodeActionProperties.CLICKABLE
            ),
            styles = setOf(
                UiNodeStyleProperties.TextStyle(
                    text = "Hello World",
                    textColor = 0xFF000000.toInt(),
                    textSize = 12f,
                    isBold = false,
                    isItalic = false,
                ),
            )
        )
    )
    ElementDetails(
        selectionState = selectionState,
        unitMode = UnitMode.DP,
        isEditMode = isEditMode,
        onEditModeChange = { isEditMode = it },
        onRequestFocusable = {},
        onApplyMarginPadding = { _, _, _, _, _, _, _, _ -> }
    )
}

@Preview(showBackground = true, widthDp = 148, heightDp = 180)
@Composable
internal fun EditMarginPaddingPreview() {
    EditMarginPadding(
        initialMargin = RectF(16f, 16f, 16f, 16f),
        initialPadding = RectF(4f, 8f, 4f, 8f),
        unitMode = UnitMode.DP,
        onCancel = {},
        onApply = { _, _, _, _, _, _, _, _ -> }
    )
}

private fun formatSizeString(
    size: android.util.Size,
    unitMode: UnitMode,
    density: Float,
): String = when (unitMode) {
    UnitMode.DP -> "${(size.width / density).roundToInt()}dp x ${(size.height / density).roundToInt()}dp"
    UnitMode.PX -> "${size.width}px x ${size.height}px"
}

private fun RectF.formatAsList(
    unitMode: UnitMode,
    density: Float,
): List<String> = when (unitMode) {
    UnitMode.DP -> listOf(
        "${(left / density).roundToInt()}dp",
        "${(top / density).roundToInt()}dp",
        "${(right / density).roundToInt()}dp",
        "${(bottom / density).roundToInt()}dp",
    )

    UnitMode.PX -> listOf(
        "${left.roundToInt()}px",
        "${top.roundToInt()}px",
        "${right.roundToInt()}px",
        "${bottom.roundToInt()}px",
    )
}
