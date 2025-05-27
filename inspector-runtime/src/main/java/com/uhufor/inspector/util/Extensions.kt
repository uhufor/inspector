package com.uhufor.inspector.util

import android.content.res.Resources
import kotlin.math.roundToInt

fun Int.dp(): Int = (this * Resources.getSystem().displayMetrics.density).roundToInt()
