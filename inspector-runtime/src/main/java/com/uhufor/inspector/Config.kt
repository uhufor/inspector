package com.uhufor.inspector

import android.content.res.Resources

class Config {
    var unitMode: UnitMode = UnitMode.DP
    val densityString: String
        get() = "%.1fx".format(Resources.getSystem().displayMetrics.density)
}
