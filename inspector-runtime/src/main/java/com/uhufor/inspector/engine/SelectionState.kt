package com.uhufor.inspector.engine

import android.graphics.Color
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Size

internal data class SelectionState(
    val id: Int,
    val bounds: RectF,
    val parentBounds: RectF?,
    val properties: UiNodeProperties,
)

internal enum class UiNodeType(val value: String) {
    VIEW("XML"),
    COMPOSE("Compose")
}

internal enum class UiNodeActionProperties(val value: String) {
    CLICKABLE("Clickable"),
    LONG_CLICKABLE("Long Clickable"),
    SELECTABLE("Selectable"),
    CHECKABLE("Checkable"),
    FOCUSABLE("Focusable"),
}

internal sealed interface UiNodeStyleProperties {
    data class ColorStyle(
        val backgroundType: String?,
        val backgroundColor: Int?,
    ) : UiNodeStyleProperties

    data class TextStyle(
        val text: String,
        val textColor: Int?,
        val textSize: Float?,
        val isBold: Boolean,
        val isItalic: Boolean,
    ) : UiNodeStyleProperties
}

internal data class UiNodeProperties(
    val type: UiNodeType,
    val id: String,
    val size: Size,
    val margin: RectF,
    val actions: Set<UiNodeActionProperties> = emptySet(),
    val styles: Set<UiNodeStyleProperties> = emptySet(),
) {
    val isClickable: Boolean
        get() = actions.contains(UiNodeActionProperties.CLICKABLE)

    val isLongClickable: Boolean
        get() = actions.contains(UiNodeActionProperties.LONG_CLICKABLE)
}
