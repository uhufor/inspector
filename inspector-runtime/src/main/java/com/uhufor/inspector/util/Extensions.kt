package com.uhufor.inspector.util

import android.content.res.Resources

internal fun Number.dp(): Float {
    return toFloat() * Resources.getSystem().displayMetrics.density
}
