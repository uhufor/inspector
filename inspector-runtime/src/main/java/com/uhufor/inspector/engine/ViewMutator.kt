package com.uhufor.inspector.engine

import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView

internal object ViewMutator {

    fun setMarginById(id: Int, left: Int, top: Int, right: Int, bottom: Int) {
        val v = UiNodeViewRegistry.get(id) ?: return
        val lp = v.layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            lp.setMargins(left, top, right, bottom)
            v.layoutParams = lp
            v.requestLayout()
            v.invalidate()
        }
    }

    fun setPaddingById(id: Int, left: Int, top: Int, right: Int, bottom: Int) {
        val v = UiNodeViewRegistry.get(id) ?: return
        v.setPadding(left, top, right, bottom)
        v.requestLayout()
        v.invalidate()
    }

    fun setTextById(id: Int, text: CharSequence) {
        val v = UiNodeViewRegistry.get(id) ?: return
        if (v is TextView) {
            v.text = text
            v.requestLayout()
            v.invalidate()
        }
    }

    fun setTextSizeSpById(id: Int, size: Float) {
        val v = UiNodeViewRegistry.get(id) ?: return
        if (v is TextView) {
            v.textSize = size
            v.requestLayout()
            v.invalidate()
        }
    }

    fun setTextColorById(id: Int, color: Int) {
        val v = UiNodeViewRegistry.get(id) ?: return
        if (v is TextView) {
            v.setTextColor(color)
            v.invalidate()
        }
    }

    fun runAfterNextLayout(id: Int, action: () -> Unit) {
        val v = UiNodeViewRegistry.get(id) ?: return
        val vto = v.viewTreeObserver
        val listener = object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (v.viewTreeObserver.isAlive) {
                    v.viewTreeObserver.removeOnPreDrawListener(this)
                }
                action()
                return true
            }
        }
        vto.addOnPreDrawListener(listener)
    }
}
