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
    private const val INVALID_AREA = Int.MAX_VALUE
    private const val MIN_ELEMENT_SIZE = 1

    fun hitTest(root: View, x: Int, y: Int): Pair<RectF, Boolean>? {
        val composeViews = mutableListOf<AbstractComposeView>()
        collectComposeViews(root, composeViews)

        var bestMatch: Pair<RectF, Boolean>? = null
        var smallestArea = INVALID_AREA

        composeViews.forEach { composeView ->
            val owner = (composeView as? RootForTest)?.semanticsOwner ?: return@forEach
            val nodes = owner.getAllSemanticsNodes(mergingEnabled = true)

            nodes.forEach { node ->
                val bounds = node.boundsInWindow
                if (bounds.width > MIN_ELEMENT_SIZE && bounds.height > MIN_ELEMENT_SIZE) {
                    if (bounds.contains(Offset(x.toFloat(), y.toFloat()))) {
                        val area = (bounds.width * bounds.height).toInt()
                        if (area < smallestArea) {
                            val isClickable = isNodeClickable(node)
                            smallestArea = area
                            bestMatch = RectF(
                                bounds.left,
                                bounds.top,
                                bounds.right,
                                bounds.bottom
                            ) to isClickable
                        }
                    }
                }
            }
        }

        return bestMatch
    }

    fun scanAllElements(root: View): List<SelectionState> {
        val composeViews = mutableListOf<AbstractComposeView>()
        collectComposeViews(root, composeViews)

        return composeViews.flatMap { composeView ->
            val owner = (composeView as? RootForTest)?.semanticsOwner ?: return@flatMap emptyList()
            owner.getAllSemanticsNodes(mergingEnabled = true)
                .filter { node ->
                    val bounds = node.boundsInWindow
                    bounds.width > MIN_ELEMENT_SIZE && bounds.height > MIN_ELEMENT_SIZE
                }
                .map { node ->
                    val bounds = node.boundsInWindow
                    SelectionState(
                        bounds = RectF(
                            bounds.left,
                            bounds.top,
                            bounds.right,
                            bounds.bottom
                        ),
                        isClickable = isNodeClickable(node)
                    )
                }
        }
    }

    private fun collectComposeViews(view: View, out: MutableList<AbstractComposeView>) {
        if (view is AbstractComposeView) {
            out.add(view)
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectComposeViews(view.getChildAt(i), out)
            }
        }
    }

    private fun isNodeClickable(node: androidx.compose.ui.semantics.SemanticsNode): Boolean {
        return node.config.getOrNull(SemanticsProperties.Role) != null ||
                node.config.getOrNull(SemanticsActions.OnClick) != null ||
                node.config.getOrNull(SemanticsActions.OnLongClick) != null
    }
}
