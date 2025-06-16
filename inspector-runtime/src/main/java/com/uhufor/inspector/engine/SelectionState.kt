package com.uhufor.inspector.engine

import android.graphics.Color
import android.graphics.RectF
import android.util.Size

internal data class SelectionState(
    val id: Int,
    val bounds: RectF,
    val parentBounds: RectF?,
    val properties: UiNodeProperties,
)

internal enum class UiNodeType(val value: String) {
    VIEW("View"),
    COMPOSE("Compose")
}

internal enum class UiNodeActionProperties(val value: String) {
    CLICKABLE("Clickable"),
    LONG_CLICKABLE("Long Clickable"),
    SELECTABLE("Selectable"),
    CHECKABLE("Checkable"),
    FOCUSABLE("Focusable"),
}

internal sealed class UiNodeStyleProperties(
    open val backgroundColor: Int,
) {
    data class ColorStyle(
        override val backgroundColor: Int,
    ) : UiNodeStyleProperties(backgroundColor)

    data class TextStyle(
        val text: String,
        val textColor: Int,
        override val backgroundColor: Int = Color.TRANSPARENT,
    ) : UiNodeStyleProperties(backgroundColor)
}

internal sealed class UiNodeProperties(
    val type: UiNodeType,
    open val id: String,
    open val size: Size,
    open val margin: RectF,
    open val actions: Set<UiNodeActionProperties>,
    open val styles: Set<UiNodeStyleProperties>,
) {
    val isClickable: Boolean
        get() = actions.contains(UiNodeActionProperties.CLICKABLE)

    val isLongClickable: Boolean
        get() = actions.contains(UiNodeActionProperties.LONG_CLICKABLE)

    data class ViewNodeProperties(
        override val id: String,
        override val size: Size,
        override val margin: RectF,
        override val actions: Set<UiNodeActionProperties> = emptySet(),
        override val styles: Set<UiNodeStyleProperties> = emptySet(),
    ) : UiNodeProperties(UiNodeType.VIEW, id, size, margin, actions, styles)

    data class ComposeNodeProperties(
        override val id: String,
        override val size: Size,
        override val margin: RectF,
        override val actions: Set<UiNodeActionProperties> = emptySet(),
        override val styles: Set<UiNodeStyleProperties> = emptySet(),
    ) : UiNodeProperties(UiNodeType.COMPOSE, id, size, margin, actions, styles)
}
