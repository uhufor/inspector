package com.uhufor.inspector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.Gravity
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.uhufor.inspector.ui.TriggerLayout
import com.uhufor.inspector.util.FloatingViewDragHelper
import com.uhufor.inspector.util.FloatingViewDragHelperDelegate
import com.uhufor.inspector.util.ScreenSizeProvider
import java.lang.ref.WeakReference

internal class FloatingTrigger(
    context: Context,
    private val inspector: Inspector,
) {
    private val context: WeakReference<Context> = WeakReference(context)
    private val windowManager: WeakReference<WindowManager> =
        WeakReference(context.getSystemService())

    private var triggerLayout: TriggerLayout? = null
    private var triggerLayoutParams: WindowManager.LayoutParams? = null
    private var dragHelper: FloatingViewDragHelper? = null

    private var isInstalled = false

    @SuppressLint("ClickableViewAccessibility")
    fun install() {
        if (isInstalled) return
        val currentContext = context.get() ?: return
        val currentWindowManager = windowManager.get() ?: return

        val triggerLayout = TriggerLayout(currentContext).also { this.triggerLayout = it }
        val triggerLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }.also {
            this.triggerLayoutParams = it
        }
        updateTriggerLayoutEnableState(triggerLayout)
        triggerLayout.setOnButtonClickListener { buttonType ->
            when (buttonType) {
                TriggerLayout.ButtonType.INSPECTION -> {
                    inspector.toggleInspection()
                }

                TriggerLayout.ButtonType.DP -> {
                    inspector.setUnitMode(
                        if (inspector.getUnitMode() == UnitMode.DP) {
                            UnitMode.PX
                        } else {
                            UnitMode.DP
                        }
                    )
                }

                TriggerLayout.ButtonType.DFS -> {
                    inspector.setTraverseType(
                        if (inspector.getTraverseType() == TraverseType.DFS) {
                            TraverseType.HIERARCHICAL
                        } else {
                            TraverseType.DFS
                        }
                    )
                }
            }
            updateTriggerLayoutEnableState(triggerLayout)
        }

        val currentDragHelper = FloatingViewDragHelper(
            screenSizeProvider = object : ScreenSizeProvider {
                override fun getSize(): Size {
                    return this@FloatingTrigger.getScreenSize()
                }
            },
            delegate = object : FloatingViewDragHelperDelegate {
                override fun getPosition(): Pair<Int, Int> {
                    return Pair(triggerLayoutParams.x, triggerLayoutParams.y)
                }

                override fun getSize(): Size {
                    return Size(
                        this@FloatingTrigger.triggerLayout?.width ?: 0,
                        this@FloatingTrigger.triggerLayout?.height ?: 0
                    )
                }

                override fun onChangePosition(
                    x: Int,
                    y: Int,
                ) {
                    triggerLayoutParams.x = x
                    triggerLayoutParams.y = y
                    currentWindowManager.updateViewLayout(
                        this@FloatingTrigger.triggerLayout,
                        triggerLayoutParams
                    )
                }
            },
        ).also {
            dragHelper = it
        }
        triggerLayout.setFloatingViewDragHelper(currentDragHelper)

        runCatching {
            currentWindowManager.addView(triggerLayout, triggerLayoutParams)
            isInstalled = true
        }.onFailure {
            isInstalled = false
        }
    }

    private fun updateTriggerLayoutEnableState(triggerLayout: TriggerLayout) {
        triggerLayout.setButtonEnableState(
            TriggerLayout.ButtonType.INSPECTION,
            inspector.isInspectionEnabled
        )
        triggerLayout.setButtonEnableState(
            TriggerLayout.ButtonType.DP,
            inspector.getUnitMode() == UnitMode.DP
        )
        triggerLayout.setButtonEnableState(
            TriggerLayout.ButtonType.DFS,
            inspector.getTraverseType() == TraverseType.DFS
        )
    }

    private fun getScreenSize(): Size {
        val wm = windowManager.get() ?: return Size(0, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            Size(windowMetrics.bounds.width(), windowMetrics.bounds.height())
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getMetrics(displayMetrics)
            Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun uninstall() {
        if (!isInstalled) return

        runCatching {
            windowManager.get()?.removeView(triggerLayout)
        }
        triggerLayout = null
        triggerLayoutParams = null
        dragHelper = null
        isInstalled = false
    }

    fun bringToFront() {
        if (!isInstalled || triggerLayout == null || triggerLayoutParams == null) return

        runCatching {
            windowManager.get()?.removeView(triggerLayout)
            windowManager.get()?.addView(triggerLayout, triggerLayoutParams)
        }
    }

    fun refreshEnableState() {
        triggerLayout?.let(::updateTriggerLayoutEnableState)
    }
}
