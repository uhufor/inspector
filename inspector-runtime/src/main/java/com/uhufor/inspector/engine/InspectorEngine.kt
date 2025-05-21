package com.uhufor.inspector.engine

import android.app.Activity
import android.app.Application
import android.graphics.Rect
import android.graphics.RectF
import android.view.View

data class SelectionState(val bounds: RectF, val isClickable: Boolean = false)

internal class InspectorEngine(
    app: Application,
    private val invalidator: () -> Unit,
) {

    var selection: SelectionState? = null
        private set
        
    var allElements: List<SelectionState> = emptyList()
        private set

    fun handleTap(x: Float, y: Float) {
        val act = topActivity() ?: return
        val root = act.window.decorView

        ComposeHitTester.hitTest(root, x.toInt(), y.toInt())?.let { (rect, isClickable) ->
            selection = SelectionState(rect, isClickable)
            invalidator()
            return
        }

        ViewHitTester.findLeaf(root, x.toInt(), y.toInt())?.let { v ->
            val r = Rect()
            v.getGlobalVisibleRect(r)
            selection = SelectionState(RectF(r), v.isClickable || v.isLongClickable)
            invalidator()
        }
    }

    private fun topActivity(): Activity? = ActivityTracker.top
    
    fun scanAllElements() {
        val act = topActivity() ?: return
        val root = act.window.decorView
        
        val elements = mutableListOf<SelectionState>()
        
        // Scan Compose elements
        val composeElements = ComposeHitTester.scanAllElements(root)
        elements.addAll(composeElements)
        
        // Scan View elements
        val viewElements = ViewHitTester.scanAllElements(root)
        elements.addAll(viewElements)
        
        allElements = elements
        invalidator()
    }
    
    fun clearScan() {
        allElements = emptyList()
        selection = null
        invalidator()
    }

    init {
        ActivityTracker.register(app)
    }
}
