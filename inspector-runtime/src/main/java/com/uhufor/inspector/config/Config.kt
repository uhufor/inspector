package com.uhufor.inspector.config

import android.content.res.Resources
import com.uhufor.inspector.UnitMode

class Config {
    var unitMode: UnitMode = UnitMode.DP
    val densityString: String
        get() = "%.1fx".format(Resources.getSystem().displayMetrics.density)
}
