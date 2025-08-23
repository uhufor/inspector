package com.uhufor.inspector.util

import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager

internal fun Number.dp(): Float {
    return toFloat() * Resources.getSystem().displayMetrics.density
}

internal fun WindowManager.getScreenSize(): Size {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val metrics = this.currentWindowMetrics
        Size(metrics.bounds.width(), metrics.bounds.height())
    } else {
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        this.defaultDisplay.getMetrics(dm)
        Size(dm.widthPixels, dm.heightPixels)
    }
}
