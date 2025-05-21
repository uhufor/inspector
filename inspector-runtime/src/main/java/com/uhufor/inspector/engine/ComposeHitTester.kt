package com.uhufor.inspector.engine

import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull

internal object ComposeHitTester {

    fun hitTest(root: View, x: Int, y: Int): Pair<RectF, Boolean>? {
        val composeViews = mutableListOf<AbstractComposeView>()
        collect(root, composeViews)

        var best: Pair<RectF, Boolean>? = null
        var bestArea = Int.MAX_VALUE

        for (cv in composeViews) {
            val owner = (cv as? RootForTest)?.semanticsOwner ?: continue
            val nodes = owner.getAllSemanticsNodes(mergingEnabled = true)
            nodes.forEach { n ->
                val b = n.boundsInWindow
                if (b.contains(offset = Offset(x.toFloat(), y.toFloat()))) {
                    val area = (b.width * b.height).toInt()
                    if (area < bestArea) {
                        val isClickable = n.config.getOrNull(SemanticsProperties.Role) != null ||
                                n.config.getOrNull(SemanticsActions.OnClick) != null ||
                                n.config.getOrNull(SemanticsActions.OnLongClick) != null
                        bestArea = area
                        best = Pair(RectF(b.left, b.top, b.right, b.bottom), isClickable)
                    }
                }
            }
        }
        return best
    }

    private fun collect(v: View, out: MutableList<AbstractComposeView>) {
        if (v is AbstractComposeView) out += v
        if (v is ViewGroup) for (i in 0 until v.childCount) collect(v.getChildAt(i), out)
    }
    
    fun scanAllElements(root: View): List<SelectionState> {
        val composeViews = mutableListOf<AbstractComposeView>()
        collect(root, composeViews)
        
        val elements = mutableListOf<SelectionState>()
        
        for (cv in composeViews) {
            val owner = (cv as? RootForTest)?.semanticsOwner ?: continue
            val nodes = owner.getAllSemanticsNodes(mergingEnabled = true)
            
            nodes.forEach { n ->
                val b = n.boundsInWindow
                // Skip empty or very small elements
                if (b.width > 1 && b.height > 1) {
                    val isClickable = n.config.getOrNull(SemanticsProperties.Role) != null ||
                            n.config.getOrNull(SemanticsActions.OnClick) != null ||
                            n.config.getOrNull(SemanticsActions.OnLongClick) != null
                    elements.add(SelectionState(RectF(b.left, b.top, b.right, b.bottom), isClickable))
                }
            }
        }
        
        return elements
    }
}
