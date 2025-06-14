package com.uhufor.inspector.engine

import android.graphics.RectF
import android.util.Size

data class SelectionState(
    val id: Int,
    val bounds: RectF,
    val parentBounds: RectF?,
    val properties: UiNodeProperties,
)

enum class UiNodeType(val value: String) {
    VIEW("View"),
    COMPOSE("Compose")
}

enum class UiNodeActionProperties(val value: String) {
    CLICKABLE("Clickable"),
    LONG_CLICKABLE("Long Clickable"),
    SELECTABLE("Selectable"),
    CHECKABLE("Checkable"),
    FOCUSABLE("Focusable"),
}

sealed class UiNodeProperties(
    val type: UiNodeType,
    open val id: String,
    open val size: Size,
    open val margin: RectF,
    open val actions: Set<UiNodeActionProperties>,
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
    ) : UiNodeProperties(UiNodeType.VIEW, id, size, margin, actions)

    data class ComposeNodeProperties(
        override val id: String,
        override val size: Size,
        override val margin: RectF,
        override val actions: Set<UiNodeActionProperties> = emptySet(),
    ) : UiNodeProperties(UiNodeType.COMPOSE, id, size, margin, actions)
}
