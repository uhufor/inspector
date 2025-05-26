package com.uhufor.inspector

import android.app.Application
import androidx.annotation.MainThread
import com.uhufor.inspector.util.ActivityTracker

object Inspector {

    @JvmField
    val config: Config = Config()
    private var installed = false

    @MainThread
    fun install(app: Application) {
        if (installed) return
        installed = true

        ActivityTracker.register(app)
        FloatingTrigger.install(app)
    }
}

class Config {
    var unitMode: UnitMode = UnitMode.DP
    internal var densityString: String = ""
}

enum class UnitMode { DP, PX }
