package com.uhufor.inspector.engine

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

internal object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var topReference: WeakReference<Activity>? = null
    val top: Activity? get() = topReference?.get()

    override fun onActivityStarted(activity: Activity) {
        topReference = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        topReference = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    fun register(app: Application) = app.registerActivityLifecycleCallbacks(this)
}
