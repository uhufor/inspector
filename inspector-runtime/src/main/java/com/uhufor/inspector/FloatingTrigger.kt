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
import com.uhufor.inspector.ui.TriggerLayout
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
        triggerLayout.setOnClickAction { inspector.toggleInspection() }
        triggerLayout.setOnLongClickAction { showMenu(triggerLayout) }

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
            updateButtonLabel(inspector.isInspectionEnabled)
            isInstalled = true
        }.onFailure {
            isInstalled = false
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
        if (!isInstalled || triggerLayout == null) return

        val config = configProvider.getConfig()
        triggerLayout?.setLabelText(
            "${config.unitMode.name} (${config.densityString})\n > ${if (isOverlayEnabled) "ON" else "OFF"}"
        )
    }

    fun updateInspectorState(isOverlayEnabled: Boolean) {
        updateButtonLabel(isOverlayEnabled)
    }

    fun bringToFront() {
        if (!isInstalled || triggerLayout == null || triggerLayoutParams == null) return

        runCatching {
            windowManager.get()?.removeView(triggerLayout)
            windowManager.get()?.addView(triggerLayout, triggerLayoutParams)
        }
    }

    companion object {
        private const val MENU_ID_SWITCH_UNIT = 1
        private const val MENU_ID_TOGGLE_OVERLAY = 2
        private const val MENU_ID_TOGGLE_DFS_TRAVERSE = 3
    }
}
