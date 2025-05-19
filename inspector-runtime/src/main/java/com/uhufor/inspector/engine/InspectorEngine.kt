package com.uhufor.inspector.engine

import android.app.Activity
import android.app.Application
import android.graphics.Rect
import android.graphics.RectF

data class SelectionState(val bounds: RectF)

internal class InspectorEngine(
    private val app: Application,
    private val invalidator: () -> Unit
) {

    var selection: SelectionState? = null
        private set

    fun handleTap(x: Float, y: Float) {
        val act = topActivity() ?: return
        val root = act.window.decorView

        ComposeHitTester.hitTest(root, x.toInt(), y.toInt())?.let {
            selection = SelectionState(it)
            invalidator()
            return
        }

        ViewHitTester.findLeaf(root, x.toInt(), y.toInt())?.let { v ->
            val r = Rect()
            v.getGlobalVisibleRect(r)
            selection = SelectionState(RectF(r))
            invalidator()
        }
    }

    private fun topActivity(): Activity? = ActivityTracker.top

    init { ActivityTracker.register(app) }
}
