package com.uhufor.inspector.ui

import android.graphics.RectF
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uhufor.inspector.UnitMode
import com.uhufor.inspector.engine.SelectionState
import com.uhufor.inspector.engine.UiNodeActionProperties
import com.uhufor.inspector.engine.UiNodeProperties
import com.uhufor.inspector.engine.UiNodeStyleProperties
import com.uhufor.inspector.engine.UiNodeType
import com.uhufor.inspector.ui.compose.rememberLowDecayFling
import kotlin.math.roundToInt

@Composable
internal fun ElementDetails(
    selectionState: SelectionState,
    unitMode: UnitMode,
) {
    val density = LocalDensity.current.density

    val size = remember(selectionState.properties.size, unitMode) {
        val size = selectionState.properties.size
        when (unitMode) {
            UnitMode.DP -> {
                "${(size.width / density).roundToInt()}dp x ${(size.height / density).roundToInt()}dp"
            }

            UnitMode.PX -> {
                "${size.width}px x ${size.height}px"
            }
        }
    }

    Card(
        modifier = Modifier
            .width(168.dp)
            .height(140.dp)
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .verticalScroll(
                    state = rememberScrollState(),
                    flingBehavior = rememberLowDecayFling(friction = 0.8F)
                )
        ) {
            SectionTitle("Details")
            InfoRow("ID (${selectionState.properties.type.value})", selectionState.properties.id)
            InfoRow("Size", size)

            BoxModel(selectionState, unitMode)
            Actions(selectionState)
            Styles(selectionState)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 9.sp,
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
            .height(12.dp)
            .padding(vertical = 1.dp),
    ) {
        Text(
            text = label,
            fontSize = 8.sp,
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
            fontSize = 7.sp,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxHeight()
                .weight(weight = 1f, fill = true)
                .background(
                    Color(0xCCFEFEFE),
                    shape = RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
                .padding(horizontal = 2.dp)
        )
    }
}

@Composable
private fun BoxModel(
    selectionState: SelectionState,
    unitMode: UnitMode,
) {
    val density = LocalDensity.current.density
    val size = remember(selectionState.properties.size, unitMode) {
        val size = selectionState.properties.size
        when (unitMode) {
            UnitMode.DP -> {
                "${(size.width / density).roundToInt()}dp x ${(size.height / density).roundToInt()}dp"
            }

            UnitMode.PX -> {
                "${size.width}px x ${size.height}px"
            }
        }
    }

    val margin = remember(selectionState.properties.margin, unitMode) {
        val margin = selectionState.properties.margin
        buildList {
            when (unitMode) {
                UnitMode.DP -> {
                    add("${(margin.left / density).roundToInt()}dp")
                    add("${(margin.top / density).roundToInt()}dp")
                    add("${(margin.right / density).roundToInt()}dp")
                    add("${(margin.bottom / density).roundToInt()}dp")
                }

                UnitMode.PX -> {
                    add("${margin.left.roundToInt()}px")
                    add("${margin.top.roundToInt()}px")
                    add("${margin.right.roundToInt()}px")
                    add("${margin.bottom.roundToInt()}px")
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle("Measurement")
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
                Text(margin[1], fontSize = 6.sp) // margin.top
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        margin[0], // margin.left
                        fontSize = 6.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(20.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .weight(weight = 1.0f, fill = true)
                            .fillMaxWidth(0.7f)
                            .padding(horizontal = 2.dp, vertical = 4.dp)
                            .border(1.dp, Color.Gray)
                            .background(Color(0xFFCCFFCC))
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(size, fontSize = 6.sp)
                    }

                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        margin[2], // margin.right
                        fontSize = 6.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(20.dp)
                    )
                }
                Text(margin[3], fontSize = 6.sp) // margin.bottom
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

private fun Int.toHexString(): String = Integer.toHexString(this).uppercase().padStart(8, '0')

@Preview(showBackground = true)
@Composable
internal fun ElementDetailPreview() {
    val selectionState = SelectionState(
        id = 0,
        bounds = RectF(0f, 0f, 200f, 100f),
        parentBounds = RectF(0f, 0f, 400f, 200f),
        properties = UiNodeProperties(
            type = UiNodeType.VIEW,
            id = "app:id/showDetail",
            size = android.util.Size(200, 100),
            margin = RectF(11f, 10f, 5f, 5f),
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
        selectionState = selectionState, unitMode = UnitMode.DP
    )
}
