package com.uhufor.inspector.engine

import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup

internal object ViewHitTester {
    private const val MIN_VIEW_SIZE = 1
    private const val MIN_ALPHA = 0.01f

    fun hitTest(root: View, x: Int, y: Int): SelectionState? {
        val hits = mutableListOf<View>()
        depthFirstSearch(root, x, y, hits)

        val leaf = hits.minByOrNull { it.width * it.height } ?: return null
        val parent = if (leaf.parent is View) leaf.parent as View else null

        val rect = Rect()
        leaf.getGlobalVisibleRect(rect)

        val parentRect = if (parent != null) {
            val parentBounds = Rect()
            parent.getGlobalVisibleRect(parentBounds)
            RectF(parentBounds)
        } else null

        return SelectionState(
            id = leaf.hashCode(),
            bounds = RectF(rect),
            parentBounds = parentRect,
            isClickable = leaf.isClickable || leaf.isLongClickable,
        )
    }

    fun scanAllElements(root: View): List<SelectionState> {
        val elements = mutableListOf<SelectionState>()
        collectAllViews(root, elements)
        return elements
    }

    private fun depthFirstSearch(
        view: View,
        x: Int,
        y: Int,
        resultList: MutableList<View>,
    ) {
        if (!view.isShown) return

        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val (left, top) = location
        val right = left + view.width
        val bottom = top + view.height

        if (x < left || y < top || x > right || y > bottom) return

        resultList += view

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                depthFirstSearch(view.getChildAt(i), x, y, resultList)
            }
        }
    }

    private fun collectAllViews(view: View, elements: MutableList<SelectionState>) {
        if (!view.isShown ||
            view.width <= MIN_VIEW_SIZE ||
            view.height <= MIN_VIEW_SIZE ||
            view.alpha < MIN_ALPHA
        ) {
            return
        }

        val rect = Rect()
        if (view.getGlobalVisibleRect(rect)) {
            if (rect.width() > 0 && rect.height() > 0) {
                val parentRect = (view.parent as? View)?.run {
                    val parentBounds = Rect()
                    if (getGlobalVisibleRect(parentBounds) &&
                        parentBounds.width() > 0 &&
                        parentBounds.height() > 0
                    ) {
                        RectF(parentBounds)
                    } else {
                        null
                    }
                }

                elements.add(
                    SelectionState(
                        id = view.hashCode(),
                        bounds = RectF(rect),
                        parentBounds = parentRect,
                        isClickable = view.isClickable || view.isLongClickable,
                    )
                )
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectAllViews(view.getChildAt(i), elements)
            }
        }
    }
}
