package com.uhufor.inspector.util

import android.content.Context
import kotlin.math.roundToInt

fun Int.dp(context: Context): Int = (this * context.resources.displayMetrics.density).roundToInt()
