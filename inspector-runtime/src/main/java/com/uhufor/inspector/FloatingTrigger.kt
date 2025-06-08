package com.uhufor.inspector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.core.content.getSystemService
import com.uhufor.inspector.ui.TriggerButton
import com.uhufor.inspector.util.FloatingViewDragHelper
import com.uhufor.inspector.util.FloatingViewDragHelperDelegate
import com.uhufor.inspector.util.ScreenSizeProvider
import java.lang.ref.WeakReference

internal class FloatingTrigger(
    context: Context,
    private val configProvider: ConfigProvider,
    private val inspector: Inspector,
) {
    private val context: WeakReference<Context> = WeakReference(context)
    private val windowManager: WeakReference<WindowManager> =
        WeakReference(context.getSystemService())

    private var floatingView: TriggerButton? = null
    private var floatingViewLayoutParams: WindowManager.LayoutParams? = null
    private var dragHelper: FloatingViewDragHelper? = null

    private var isInstalled = false

    @SuppressLint("ClickableViewAccessibility")
    fun install() {
        if (isInstalled) return
        val currentContext = context.get() ?: return
        val currentWindowManager = windowManager.get() ?: return

        val triggerButton = TriggerButton(currentContext).also { floatingView = it }
        val triggerButtonLayoutParams = WindowManager.LayoutParams(
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
            floatingViewLayoutParams = it
        }

        val currentDragHelper = FloatingViewDragHelper(
            screenSizeProvider = object : ScreenSizeProvider {
                override fun getSize(): Size {
                    return this@FloatingTrigger.getScreenSize()
                }
            },
            delegate = object : FloatingViewDragHelperDelegate {
                override fun getPosition(): Pair<Int, Int> {
                    return Pair(triggerButtonLayoutParams.x, triggerButtonLayoutParams.y)
                }

                override fun getSize(): Size {
                    return Size(floatingView?.width ?: 0, floatingView?.height ?: 0)
                }

                override fun onChangePosition(
                    x: Int,
                    y: Int,
                ) {
                    triggerButtonLayoutParams.x = x
                    triggerButtonLayoutParams.y = y
                    currentWindowManager.updateViewLayout(floatingView, triggerButtonLayoutParams)
                }
            },
        ).also {
            dragHelper = it
        }

        try {
            triggerButton.setOnClickAction { inspector.toggleInspection() }
            triggerButton.setOnLongClickAction { showMenu(triggerButton) }
            triggerButton.setDragHelperInstance(currentDragHelper)

            currentWindowManager.addView(triggerButton, triggerButtonLayoutParams)
            updateButtonLabel(inspector.isInspectionEnabled)
            isInstalled = true
        } catch (_: Exception) {
            isInstalled = false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun uninstall() {
        if (!isInstalled) return

        floatingView?.let {
            runCatching {
                windowManager.get()?.removeView(it)
            }
        }
        floatingView = null
        dragHelper = null
        isInstalled = false
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

    // TODO: This functions should be move to each icon on the floating view
    private fun showMenu(anchor: View) {
        val currentConfig = configProvider.getConfig()
        val isOverlayCurrentlyShown = inspector.isInspectionEnabled
        val isDfsTraverseEnabled = inspector.isDfsTraverseEnabled

        PopupMenu(anchor.context, anchor).apply {
            menu.add(
                0,
                MENU_ID_SWITCH_UNIT,
                0,
                "Switch to ${if (currentConfig.unitMode == UnitMode.DP) "PX" else "DP"}"
            )
            menu.add(
                0,
                MENU_ID_TOGGLE_OVERLAY,
                1,
                if (isOverlayCurrentlyShown) "Hide overlay" else "Show overlay"
            )
            menu.add(
                0,
                MENU_ID_TOGGLE_DFS_TRAVERSE,
                1,
                if (isDfsTraverseEnabled) "Disable DFS Traverse" else "Enable DFS Traverse"
            )

            setOnMenuItemClickListener {
                when (it.itemId) {
                    MENU_ID_SWITCH_UNIT -> {
                        val newMode = if (currentConfig.unitMode == UnitMode.DP) {
                            UnitMode.PX
                        } else {
                            UnitMode.DP
                        }
                        inspector.setUnitMode(newMode)
                    }

                    MENU_ID_TOGGLE_OVERLAY -> {
                        inspector.toggleInspection()
                    }

                    MENU_ID_TOGGLE_DFS_TRAVERSE -> {
                        if (isDfsTraverseEnabled) {
                            inspector.disableDfsTraverse()
                        } else {
                            inspector.enableDfsTraverse()
                        }
                    }
                }
                true
            }
            show()
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateButtonLabel(isOverlayEnabled: Boolean) {
        if (!isInstalled || floatingView == null) return

        val config = configProvider.getConfig()
        floatingView?.setLabelText(
            "${config.unitMode.name} (${config.densityString})\n > ${if (isOverlayEnabled) "ON" else "OFF"}"
        )
    }

    fun updateInspectorState(isOverlayEnabled: Boolean) {
        updateButtonLabel(isOverlayEnabled)
    }

    fun bringToFront() {
        if (!isInstalled || floatingView == null || floatingViewLayoutParams == null) return

        runCatching {
            windowManager.get()?.removeView(floatingView)
            windowManager.get()?.addView(floatingView, floatingViewLayoutParams)
        }
    }

    companion object {
        private const val MENU_ID_SWITCH_UNIT = 1
        private const val MENU_ID_TOGGLE_OVERLAY = 2
        private const val MENU_ID_TOGGLE_DFS_TRAVERSE = 3
    }
}
