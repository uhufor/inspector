package com.uhufor.inspector.engine

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.RectF

data class SelectionState(
    val bounds: RectF,
    val isClickable: Boolean = false,
    val parentBounds: RectF? = null,
)

internal class InspectorEngine(
    context: Context,
    private val invalidator: () -> Unit,
) {
    var selection: SelectionState? = null
        private set

    var allElements: List<SelectionState> = emptyList()
        private set

    init {
        ActivityTracker.register(context)
    }

    fun handleTap(x: Float, y: Float) {
        val activity = topActivity() ?: return
        val rootView = activity.window.decorView

        ComposeHitTester.hitTest(rootView, x.toInt(), y.toInt())?.let { (rect, isClickable) ->
            selection = SelectionState(rect, isClickable)
            invalidate()
            return
        }

        ViewHitTester.findLeaf(rootView, x.toInt(), y.toInt())?.let { (view, parentView) ->
            val rect = Rect()
            view.getGlobalVisibleRect(rect)

            val parentRect = if (parentView != null) {
                val parentBounds = Rect()
                parentView.getGlobalVisibleRect(parentBounds)
                RectF(parentBounds)
            } else null

            selection = SelectionState(
                bounds = RectF(rect),
                isClickable = view.isClickable || view.isLongClickable,
                parentBounds = parentRect
            )
            invalidate()
        }
    }

    fun scanAllElements() {
        val activity = topActivity() ?: return
        val rootView = activity.window.decorView

        val elements = mutableListOf<SelectionState>()

        elements.addAll(ComposeHitTester.scanAllElements(rootView))
        elements.addAll(ViewHitTester.scanAllElements(rootView))

        allElements = elements
        invalidate()
    }

    fun clearScan() {
        allElements = emptyList()
        selection = null
        invalidate()
    }

    private fun topActivity(): Activity? = ActivityTracker.top

    private fun invalidate() {
        invalidator()
    }
}
