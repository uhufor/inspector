package com.uhufor.inspector.engine

import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.ViewGroup

internal object ViewHitTester {

    fun findLeaf(root: View, x: Int, y: Int): View? {
        val hits = mutableListOf<View>()
        dfs(root, x, y, hits)
        return hits.minByOrNull { it.width * it.height }
    }

    private fun dfs(v: View, x: Int, y: Int, l: MutableList<View>) {
        if (!v.isShown) return
        val loc = IntArray(2)
        v.getLocationOnScreen(loc)
        if (x < loc[0] || y < loc[1] || x > loc[0] + v.width || y > loc[1] + v.height) return
        l += v
        if (v is ViewGroup) for (i in 0 until v.childCount) dfs(v.getChildAt(i), x, y, l)
    }
    
    fun scanAllElements(root: View): List<SelectionState> {
        val elements = mutableListOf<SelectionState>()
        collectAllViews(root, elements)
        return elements
    }
    
    private fun collectAllViews(v: View, elements: MutableList<SelectionState>) {
        if (!v.isShown) return
        
        if (v.width <= 1 || v.height <= 1 || v.alpha <= 0) return
        
        val r = Rect()
        if (v.getGlobalVisibleRect(r)) {
            if (r.width() > 0 && r.height() > 0) {
                elements.add(SelectionState(RectF(r), v.isClickable || v.isLongClickable))
            }
        }
        
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                collectAllViews(v.getChildAt(i), elements)
            }
        }
    }
}
