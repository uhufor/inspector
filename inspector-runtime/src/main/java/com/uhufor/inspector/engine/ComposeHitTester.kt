package com.uhufor.inspector.engine

import android.annotation.SuppressLint
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.node.RootForTest
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull

internal object ComposeHitTester {
    private const val INVALID_AREA = Int.MAX_VALUE
    private const val MIN_ELEMENT_SIZE = 1

    @SuppressLint("UseKtx")
    fun hitTest(root: View, x: Int, y: Int): SelectionState? {
        val composeViews = mutableListOf<AbstractComposeView>()
        collectComposeViews(root, composeViews)

        var matched: SelectionState? = null
        var smallestArea = INVALID_AREA

        composeViews
            .mapNotNull { composeView ->
                if (composeView.childCount == 0) return null
                (composeView.getChildAt(0) as? RootForTest)?.semanticsOwner
            }
            .forEach { owner ->
                owner
                    .getAllSemanticsNodes(mergingEnabled = false)
                    .forEach { node: SemanticsNode ->
                        val bounds = node.boundsInWindow
                        if (bounds.width > MIN_ELEMENT_SIZE &&
                            bounds.height > MIN_ELEMENT_SIZE
                        ) {
                            val hitPosition = Offset(x.toFloat(), y.toFloat())
                            if (bounds.contains(hitPosition)) {
                                val area = (bounds.width * bounds.height).toInt()
                                if (area < smallestArea) {
                                    matched = SelectionState(
                                        id = bounds.hashCode(),
                                        bounds = RectF(
                                            bounds.left,
                                            bounds.top,
                                            bounds.right,
                                            bounds.bottom
                                        ),
                                        parentBounds = node.parent?.boundsInWindow?.run {
                                            RectF(left, top, right, bottom)
                                        },
                                        isClickable = isNodeClickable(node),
                                    )
                                    smallestArea = area
                                }
                            }
                        }
                    }
            }

        return matched
    }

    @SuppressLint("UseKtx")
    fun scanAllElements(root: View): List<SelectionState> {
        val composeViews = mutableListOf<AbstractComposeView>()
        collectComposeViews(root, composeViews)

        return composeViews
            .mapNotNull { composeView ->
                if (composeView.childCount == 0) return@mapNotNull null
                (composeView.getChildAt(0) as? RootForTest)?.semanticsOwner
            }
            .flatMap { owner ->
                val allNodes = owner.getAllSemanticsNodes(mergingEnabled = false)
                allNodes
                    .filter { node: SemanticsNode ->
                        val bounds = node.boundsInWindow
                        bounds.width > MIN_ELEMENT_SIZE && bounds.height > MIN_ELEMENT_SIZE
                    }
                    .map { node: SemanticsNode ->
                        val bounds = node.boundsInWindow
                        SelectionState(
                            id = bounds.hashCode(),
                            bounds = RectF(
                                bounds.left,
                                bounds.top,
                                bounds.right,
                                bounds.bottom
                            ),
                            parentBounds = node.parent?.boundsInWindow?.run {
                                RectF(left, top, right, bottom)
                            },
                            isClickable = isNodeClickable(node),
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

    private fun isNodeClickable(node: SemanticsNode): Boolean {
        return node.config.getOrNull(SemanticsProperties.Role) != null ||
                node.config.getOrNull(SemanticsActions.OnClick) != null ||
                node.config.getOrNull(SemanticsActions.OnLongClick) != null
    }
}
