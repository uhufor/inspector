package com.uhufor.inspector.engine

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal object ActivityTracker : Application.ActivityLifecycleCallbacks {
    var top: Activity? = null
        private set

    override fun onActivityStarted(activity: Activity) { top = activity }
    override fun onActivityResumed(activity: Activity) { top = activity }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    fun register(app: Application) = app.registerActivityLifecycleCallbacks(this)
}
