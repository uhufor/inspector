package com.uhufor.inspector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.PopupMenu
import androidx.core.content.getSystemService
import com.uhufor.inspector.ui.TriggerButton
import com.uhufor.inspector.util.dp
import java.lang.ref.WeakReference

internal class FloatingTrigger(
    private val context: Context,
    private val inspector: Inspector,
) {
    private val windowManager: WeakReference<WindowManager?> =
        WeakReference(context.getSystemService())

    private var button: TriggerButton? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isInstalled = false

    fun install() {
        if (isInstalled) return
        button = TriggerButton(context).also {
            it.setClickListener { inspector.toggleInspection() }
            it.setLongClickListener {
                showMenu(it)
                true
            }
        }

        this.layoutParams = WindowManager.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            x = 16.dp(context)
            y = 96.dp(context)
        }
        try {
            windowManager.get()?.addView(button, this.layoutParams)
            isInstalled = true
        } catch (e: Exception) {
            isInstalled = false
        }
        updateButtonLabel(inspector.isInspectionEnabled, inspector.getConfig().unitMode)
    }

    fun uninstall() {
        if (!isInstalled) return
        button?.let {
            runCatching {
                windowManager.get()?.removeView(it)
            }
        }
        button = null
        isInstalled = false
    }

    private fun showMenu(anchor: View) {
        val currentConfig = inspector.getConfig()
        val isOverlayCurrentlyShown = inspector.isInspectionEnabled

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
                }
                true
            }
            show()
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateButtonLabel(isOverlayEnabled: Boolean, unitMode: UnitMode) {
        if (!isInstalled || button == null) return

        button?.setLabelText("${unitMode.name.lowercase()} | ${if (isOverlayEnabled) "ON" else "OFF"}")
    }

    fun updateInspectorState(isOverlayEnabled: Boolean, unitMode: UnitMode) {
        updateButtonLabel(isOverlayEnabled, unitMode)
    }

    fun bringToFront() {
        if (!isInstalled || button == null || layoutParams == null) return
        runCatching {
            windowManager.get()?.removeView(button)
            windowManager.get()?.addView(button, layoutParams)
        }
    }

    companion object {
        private const val MENU_ID_SWITCH_UNIT = 1
        private const val MENU_ID_TOGGLE_OVERLAY = 2
    }
}
