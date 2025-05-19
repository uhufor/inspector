package com.uhufor.inspector.engine

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView

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
}
