package com.uhufor.inspector.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.lang.ref.WeakReference

internal object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var topReference: WeakReference<Activity>? = null

    val top: Activity?
        get() = topReference?.get()

    fun register(context: Context) {
        (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(this)

        if (context is Activity) {
            updateTopActivity(context)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        updateTopActivity(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        updateTopActivity(activity)
    }

    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    private fun updateTopActivity(activity: Activity) {
        topReference = WeakReference(activity)
    }
}
