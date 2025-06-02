package com.uhufor.inspector.util

import android.content.res.Resources

fun Number.dp(): Float {
    return toFloat() * Resources.getSystem().displayMetrics.density
}
