package com.uhufor.inspector

import android.app.Application
import androidx.annotation.MainThread

object Inspector {

    @JvmField val config: Config = Config()
    private var installed = false

    @MainThread
    fun install(app: Application) {
        if (installed) return
        installed = true
        FloatingTrigger.install(app, config)
    }
}

class Config {
    var unitMode: UnitMode = UnitMode.DP
    internal var densityString: String = ""
}

enum class UnitMode { DP, PX }
