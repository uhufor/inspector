package com.uhufor.inspectionsample

import android.app.Application
import com.uhufor.inspector.Inspector

class InspectionApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        setupDebuggingTools()
    }

    private fun setupDebuggingTools() {
        Inspector.install(this)
    }
}
