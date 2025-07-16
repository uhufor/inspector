package com.uhufor.inspector.util

import android.annotation.SuppressLint
import android.view.View
import java.lang.reflect.Field

@SuppressLint("PrivateApi")
internal object WindowManagerUtils {

    /**
     * @see [android.view.WindowManagerGlobal]
     */
    private val windowManager: Any? by lazy {
        Class
            .forName("android.view.WindowManagerGlobal")
            .getMethod("getInstance")
            .invoke(null)
    }

    private val viewsField: Field? by lazy {
        val field = windowManager?.javaClass?.getDeclaredField("mViews")
        field?.isAccessible = true
        field
    }

    @Suppress("UNCHECKED_CAST")
    fun getDecorViews(): List<View>? {
        val decorViews = viewsField?.get(windowManager) as? ArrayList<View> ?: return null
        return decorViews
            .filter { it.javaClass.simpleName.equals("DecorView") && it.isShown }
            .sortedBy { it.z }
    }
}
