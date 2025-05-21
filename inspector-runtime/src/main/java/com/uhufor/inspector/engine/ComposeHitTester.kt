package com.uhufor.inspector.engine

import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.semantics.getAllSemanticsNodes

internal object ComposeHitTester {

    fun hitTest(root: View, x: Int, y: Int): RectF? {
        val composeViews = mutableListOf<AbstractComposeView>()
        collect(root, composeViews)

        var best: RectF? = null
        var bestArea = Int.MAX_VALUE

        for (cv in composeViews) {
            val owner = (cv as? RootForTest)?.semanticsOwner ?: continue
            val nodes = owner.getAllSemanticsNodes(mergingEnabled = true)
            nodes.forEach { n ->
                val b = n.boundsInWindow
                if (b.contains(offset = Offset(x.toFloat(), y.toFloat()))) {
                    val area = (b.width * b.height).toInt()
                    if (area < bestArea) {
                        bestArea = area
                        best = RectF(b.left, b.top, b.right, b.bottom)
                    }
                }
            }
        }
        return best
    }

    /** View 트리에서 ComposeView 수집 */
    private fun collect(v: View, out: MutableList<AbstractComposeView>) {
        if (v is AbstractComposeView) out += v
        if (v is ViewGroup) for (i in 0 until v.childCount) collect(v.getChildAt(i), out)
    }
}
