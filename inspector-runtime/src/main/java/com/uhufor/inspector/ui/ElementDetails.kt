package com.uhufor.inspector.ui

import android.graphics.RectF
import android.util.Size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text2.BasicTextField2
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhufor.inspector.R
import com.uhufor.inspector.UnitMode
import com.uhufor.inspector.engine.SelectionState
import com.uhufor.inspector.engine.UiNodeActionProperties
import com.uhufor.inspector.engine.UiNodeProperties
import com.uhufor.inspector.engine.UiNodeStyleProperties
import com.uhufor.inspector.engine.UiNodeType
import com.uhufor.inspector.ui.components.EditFieldRow
import com.uhufor.inspector.ui.components.InfoRow
import com.uhufor.inspector.ui.components.MarginPaddingBox
import com.uhufor.inspector.ui.components.MeasurementMetricBox
import com.uhufor.inspector.ui.components.SectionHeader
import com.uhufor.inspector.ui.components.SectionTitle
import com.uhufor.inspector.ui.components.SmallButton
import com.uhufor.inspector.ui.compose.dvdp
import com.uhufor.inspector.ui.compose.dvsp
import kotlin.math.roundToInt

private val NumberKeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

@Composable
internal fun ElementDetails(
    selectionState: SelectionState,
    unitMode: UnitMode,
    isEditMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onRequestFocusable: (Boolean) -> Unit,
    onApplyMarginPadding: (Int, Int, Int, Int, Int, Int, Int, Int) -> Unit,
    onApplyText: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .width(148.dvdp)
            .height(180.dvdp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        var isTextEditing by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dvdp)
                .verticalScroll(state = rememberScrollState())
        ) {
            if (!isEditMode) {
                SectionHeader(
                    title = stringResource(R.string.inspector_title_details),
                    rightContent = {
                        if (selectionState.properties.type == UiNodeType.COMPOSE) {
                            Image(
                                painterResource(R.drawable.ic_compose),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(10.dvdp),
                            )
                        }
                    }
                )
                InfoRow(
                    stringResource(R.string.inspector_label_id),
                    selectionState.properties.id
                )

                Measurement(selectionState, unitMode)
                MarginPadding(
                    selectionState,
                    unitMode,
                    onEditRequest = {
                        isTextEditing = false
                        onRequestFocusable(true)
                        onEditModeChange(true)
                    }
                )
                TextContent(
                    selectionState = selectionState,
                    onEditRequest = {
                        isTextEditing = true
                        onRequestFocusable(true)
                        onEditModeChange(true)
                    }
                )
                Actions(selectionState)
                Styles(selectionState)
            } else {
                if (isTextEditing) {
                    val text = selectionState.properties.styles
                        .filterIsInstance<UiNodeStyleProperties.TextStyle>()
                        .firstOrNull()?.text

                    if (text != null) {
                        EditTextContent(
                            initialText = text,
                            onCancel = {
                                onRequestFocusable(false)
                                onEditModeChange(false)
                            },
                            onApply = { text ->
                                onApplyText(text.trim())
                                onRequestFocusable(false)
                                onEditModeChange(false)
                            }
                        )
                    }
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

    Spacer(modifier = Modifier.height(8.dvdp))
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(stringResource(R.string.inspector_title_distance))
        Spacer(modifier = Modifier.height(2.dvdp))

        MeasurementMetricBox(
            {
                Text(
                    text = margin[0],
                    fontSize = 8.dvsp,
                    modifier = Modifier
                        .background(colorResource(R.color.inspector_distance_margin_bg))
                        .padding(1.dvdp)
                        .align(Alignment.Center)
                )
            },
            {
                Text(
                    text = margin[1],
                    fontSize = 8.dvsp,
                    modifier = Modifier
                        .background(colorResource(R.color.inspector_distance_margin_bg))
                        .padding(1.dvdp)
                        .align(Alignment.Center)
                )
            },
            {
                Text(
                    text = margin[2],
                    fontSize = 8.dvsp,
                    modifier = Modifier
                        .background(colorResource(R.color.inspector_distance_margin_bg))
                        .padding(1.dvdp)
                        .align(Alignment.Center)
                )
            },
            {
                Text(
                    text = margin[3],
                    fontSize = 8.dvsp,
                    modifier = Modifier
                        .background(colorResource(R.color.inspector_distance_margin_bg))
                        .padding(1.dvdp)
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
                        fontSize = 8.dvsp,
                        modifier = Modifier
                            .background(colorResource(R.color.inspector_distance_size_bg))
                            .padding(2.dvdp)
                            .align(Alignment.Center)
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = colorResource(R.color.inspector_value_bg),
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(4.dvdp),
            lineColor = colorResource(R.color.inspector_distance_line)
        )
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

    Spacer(modifier = Modifier.height(8.dvdp))
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.inspector_title_margin_padding),
            rightContent = {
                if (selectionState.properties.type == UiNodeType.VIEW) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.inspector_button_bg),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable(enabled = isView, onClick = onEditRequest)
                            .padding(horizontal = 10.dvdp, vertical = 2.dvdp)
                    ) {
                        Text(
                            text = stringResource(R.string.inspector_action_edit),
                            fontSize = 8.dvsp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(2.dvdp))

        MarginPaddingBox(
            margin = margin,
            padding = padding,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Actions(selectionState: SelectionState) {
    if (selectionState.properties.actions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dvdp))
        SectionTitle(stringResource(R.string.inspector_title_actions))
        selectionState.properties.actions.forEach { action ->
            InfoRow(
                action.value,
                stringResource(R.string.inspector_value_true)
            )
        }
    }
}

@Composable
private fun Styles(selectionState: SelectionState) {
    val styles = selectionState.properties.styles.filterNot {
        it is UiNodeStyleProperties.TextStyle
    }

    if (styles.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dvdp))
        SectionTitle(stringResource(R.string.inspector_title_styles))
        styles.forEach { style ->
            when (style) {
                is UiNodeStyleProperties.ColorStyle -> {
                    style.backgroundType?.let { backgroundType ->
                        InfoRow(
                            stringResource(R.string.inspector_style_bg_type),
                            backgroundType
                        )
                    }
                    style.backgroundColor?.let { backgroundColor ->
                        InfoRow(
                            stringResource(R.string.inspector_style_bg_color),
                            "#${backgroundColor.toHexString()}"
                        )
                    }
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun TextContent(
    selectionState: SelectionState,
    onEditRequest: () -> Unit,
) {
    val style = selectionState.properties.styles
        .filterIsInstance<UiNodeStyleProperties.TextStyle>()
        .firstOrNull()
        ?: return
    val isEditable = selectionState.properties.type == UiNodeType.VIEW
    val (density, fontScale) = LocalDensity.current.run {
        density to fontScale
    }

    Spacer(modifier = Modifier.height(8.dvdp))
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            title = stringResource(R.string.inspector_style_text),
            rightContent = {
                if (isEditable) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .background(
                                color = colorResource(R.color.inspector_button_bg),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable(onClick = onEditRequest)
                            .padding(horizontal = 10.dvdp, vertical = 2.dvdp)
                    ) {
                        Text(
                            text = stringResource(R.string.inspector_action_edit),
                            fontSize = 8.dvsp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(2.dvdp))
        InfoRow(
            stringResource(R.string.inspector_style_text),
            style.text
        )
        style.textColor?.let { textColor ->
            InfoRow(
                stringResource(R.string.inspector_style_text_color),
                "#${textColor.toHexString()}"
            )
        }
        style.textSize?.let { textSize ->
            val sizeInDp = textSize / density
            val sizeInSp = sizeInDp / fontScale
            InfoRow(
                stringResource(R.string.inspector_style_text_size),
                "${sizeInSp.roundToInt()}sp, ${sizeInDp.roundToInt()}dp, ${textSize.roundToInt()}px"
            )
        }
        if (style.isBold || style.isItalic) {
            val bold = stringResource(R.string.inspector_style_bold)
            val italic = stringResource(R.string.inspector_style_italic)
            val typefaceStyles = buildList {
                if (style.isBold) add(bold)
                if (style.isItalic) add(italic)
            }
            InfoRow(
                stringResource(R.string.inspector_style_text_style),
                typefaceStyles.joinToString(", ")
            )
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
            .padding(4.dvdp)
    ) {
        SectionTitle(stringResource(R.string.inspector_title_edit_margin_padding))
        Spacer(modifier = Modifier.height(8.dvdp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(
                modifier = Modifier
                    .height(8.dvdp)
                    .weight(0.6f)
            )
            Text(
                text = stringResource(R.string.inspector_column_margin),
                fontSize = 8.dvsp,
                textAlign = TextAlign.Center,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(R.string.inspector_column_padding),
                fontSize = 8.dvsp,
                textAlign = TextAlign.Center,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(4.dvdp))

        EditFieldRow(
            label = stringResource(R.string.inspector_side_left),
            marginValue = ml,
            onMarginChange = { ml = it },
            paddingValue = pl,
            onPaddingChange = { pl = it },
            keyboardOptions = NumberKeyboardOptions,
            labelWeight = 0.6f,
            fieldWeight = 1f,
            modifier = Modifier,
        )
        EditFieldRow(
            label = stringResource(R.string.inspector_side_top),
            marginValue = mt,
            onMarginChange = { mt = it },
            paddingValue = pt,
            onPaddingChange = { pt = it },
            keyboardOptions = NumberKeyboardOptions,
            labelWeight = 0.6f,
            fieldWeight = 1f,
            modifier = Modifier,
        )
        EditFieldRow(
            label = stringResource(R.string.inspector_side_right),
            marginValue = mr,
            onMarginChange = { mr = it },
            paddingValue = pr,
            onPaddingChange = { pr = it },
            keyboardOptions = NumberKeyboardOptions,
            labelWeight = 0.6f,
            fieldWeight = 1f,
            modifier = Modifier,
        )
        EditFieldRow(
            label = stringResource(R.string.inspector_side_bottom),
            marginValue = mb,
            onMarginChange = { mb = it },
            paddingValue = pb,
            onPaddingChange = { pb = it },
            keyboardOptions = NumberKeyboardOptions,
            labelWeight = 0.6f,
            fieldWeight = 1f,
            modifier = Modifier,
        )

        Spacer(modifier = Modifier.height(2.dvdp))

        Row(modifier = Modifier.fillMaxWidth()) {
            SmallButton(
                text = stringResource(R.string.inspector_btn_cancel),
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dvdp)
            )

            Spacer(modifier = Modifier.width(4.dvdp))

            SmallButton(
                text = stringResource(R.string.inspector_btn_apply),
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
                    .padding(4.dvdp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditTextContent(
    initialText: String,
    onCancel: () -> Unit,
    onApply: (String) -> Unit,
) {
    var text by remember(initialText) { mutableStateOf(initialText) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dvdp)
    ) {
        SectionTitle(stringResource(R.string.inspector_style_text))
        Spacer(modifier = Modifier.height(8.dvdp))

        BasicTextField2(
            value = text,
            onValueChange = { text = it },
            textStyle = TextStyle(
                color = colorResource(R.color.inspector_number_field_text),
                fontSize = 9.dvsp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dvdp)
                .background(Color(0x99FFCCFF))
                .padding(8.dvdp)
        )

        Spacer(modifier = Modifier.height(4.dvdp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SmallButton(
                text = stringResource(R.string.inspector_btn_cancel),
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dvdp)
            )
            Spacer(modifier = Modifier.width(4.dvdp))
            SmallButton(
                text = stringResource(R.string.inspector_btn_apply),
                onClick = { onApply(text.trim()) },
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dvdp)
            )
        }
    }
}

private fun Int.toHexString(): String =
    Integer.toHexString(this).uppercase().padStart(8, '0')

private fun formatSizeString(
    size: Size,
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
            size = Size(200, 100),
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
        onApplyMarginPadding = { _, _, _, _, _, _, _, _ -> },
        onApplyText = { _ -> },
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
