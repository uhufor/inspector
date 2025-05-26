package com.uhufor.inspector

import android.app.Application
import androidx.annotation.MainThread
import com.uhufor.inspector.util.ActivityTracker
import com.uhufor.inspector.util.checkPermission

object Inspector {
    @JvmField
    val config: Config = Config()
    private var installed = false

    @MainThread
    fun install(app: Application) {
        if (!checkPermission(app)) {
            return
        }

        if (installed) return
        installed = true

        ActivityTracker.register(app)
        FloatingTrigger.install(app)
    }
}
