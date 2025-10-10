package com.uhufor.inspector.engine

import android.view.View
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

internal object UiNodeViewRegistry {
    private val views = ConcurrentHashMap<Int, WeakReference<View>>()

    fun register(view: View) {
        views[view.hashCode()] = WeakReference(view)
    }

    fun get(id: Int): View? = views[id]?.get()

    fun clear() {
        views.clear()
    }
}
