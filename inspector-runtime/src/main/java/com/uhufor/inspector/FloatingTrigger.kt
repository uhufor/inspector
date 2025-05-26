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
import com.uhufor.inspector.ui.OverlayCanvas
import com.uhufor.inspector.ui.OverlayCanvas.BackKeyListener
import com.uhufor.inspector.ui.TriggerButton
import com.uhufor.inspector.util.dp
import java.lang.ref.WeakReference

internal class FloatingTrigger(
    context: Context,
    private val configProvider: ConfigProvider,
) {
    private val windowManager: WeakReference<WindowManager?> =
        WeakReference(context.getSystemService())

    private var overlayShown = false
    private val overlay: OverlayCanvas = OverlayCanvas(context).apply {
        backKeyListener = object : BackKeyListener {
            override fun onBackPressed() {
                if (overlayShown) {
                    toggleOverlay()
                }
            }
        }
    }

    private lateinit var button: TriggerButton

    fun install(context: Context) {
        button = TriggerButton(context).also {
            it.setClickListener { toggleOverlay() }
            it.setLongClickListener {
                showMenu(it)
                true
            }
        }

        val layoutParams = WindowManager.LayoutParams(
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
        windowManager.get()?.addView(button, layoutParams)
        updateButtonLabel()
    }

    private fun toggleOverlay() {
        val layoutParams = button.layoutParams as WindowManager.LayoutParams
        windowManager.get()?.removeView(button)

        if (overlayShown) {
            overlay.clearScan()
            windowManager.get()?.removeView(overlay)
        } else {
            windowManager.get()?.addView(overlay, overlay.layoutParams())
            overlay.scanAllElements()
        }
        overlayShown = !overlayShown

        windowManager.get()?.addView(button, layoutParams)
    }

    private fun showMenu(anchor: View) {
        val config = configProvider.getConfig()
        PopupMenu(anchor.context, anchor).apply {
            menu.add(0, 1, 0, "Switch to ${if (config.unitMode == UnitMode.DP) "PX" else "DP"}")
            menu.add(0, 2, 1, if (overlayShown) "Hide overlay" else "Show overlay")
            setOnMenuItemClickListener {
                when (it.itemId) {
                    1 -> {
                        config.unitMode =
                            if (config.unitMode == UnitMode.DP) UnitMode.PX else UnitMode.DP
                        updateButtonLabel()
                        overlay.invalidate()
                    }

                    2 -> toggleOverlay()
                }
                true
            }
            show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateButtonLabel() {
        val config = configProvider.getConfig()
        button.setLabelText("${config.unitMode.name.lowercase()} (${config.densityString})")
    }
}
