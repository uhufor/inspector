package com.uhufor.inspector.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.uhufor.inspector.UnitMode
import com.uhufor.inspector.engine.SelectionState
import java.lang.ref.WeakReference

internal class FloatingDetailsView(context: Context) : LifecycleOwner, SavedStateRegistryOwner {
    private val context: WeakReference<Context> = WeakReference(context)
    private val windowManager: WeakReference<WindowManager> =
        WeakReference(context.getSystemService())

    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isInstalled = false

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    @SuppressLint("ClickableViewAccessibility")
    fun install(selectionState: SelectionState, unitMode: UnitMode) {
        val currentContext = context.get() ?: return
        val currentWindowManager = windowManager.get() ?: return

        if (isInstalled) {
            composeView?.setContent {
                ElementDetails(selectionState = selectionState, unitMode = unitMode)
            }
            return
        }

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        composeView = ComposeView(currentContext).apply {
            setViewTreeLifecycleOwner(this@FloatingDetailsView)
            setViewTreeSavedStateRegistryOwner(this@FloatingDetailsView)
            setContent {
                ElementDetails(selectionState = selectionState, unitMode = unitMode)
            }
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            x = 0
            y = 100
        }

        runCatching {
            currentWindowManager.addView(composeView, layoutParams)
            isInstalled = true
        }.onFailure {
            isInstalled = false
        }
    }

    fun uninstall() {
        if (!isInstalled) return
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        composeView?.let {
            it.disposeComposition()
            runCatching {
                windowManager.get()?.removeView(it)
            }
        }
        composeView = null
        layoutParams = null
        isInstalled = false
    }
}
