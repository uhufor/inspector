package com.uhufor.inspector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.getSystemService
import com.uhufor.inspector.ui.OverlayCanvas
import com.uhufor.inspector.ui.OverlayCanvas.BackKeyListener
import com.uhufor.inspector.util.dp

@SuppressLint("StaticFieldLeak")
internal object FloatingTrigger {
    private lateinit var windowManager: WindowManager
    private lateinit var overlay: OverlayCanvas
    private lateinit var configProvider: ConfigProvider
    private lateinit var fabContainer: ViewGroup
    private var overlayShown = false

    fun install(context: Context) {
        windowManager = context.getSystemService()!!

        overlay = OverlayCanvas(context).apply {
            backKeyListener = object : BackKeyListener {
                override fun onBackPressed() {
                    if (overlayShown) {
                        toggleOverlay()
                    }
                }
            }
        }

        configProvider = context.configProvider()
        with(configProvider.getConfig()) {
            densityString = "%.2fx".format(context.resources.displayMetrics.density)
        }

        fabContainer = buildFab(context)
        val lp = WindowManager.LayoutParams(
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
        windowManager.addView(fabContainer, lp)

        updateLabel()
    }

    private fun buildFab(context: Context): ViewGroup = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL

        val button = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_search)
            setOnClickListener { toggleOverlay() }
            setOnLongClickListener {
                showMenu(this)
                true
            }
        }
        val label = TextView(context).apply {
            id = View.generateViewId()
            textSize = 9F
            setTextColor(Color.RED)
        }
        addView(button, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER))
        addView(label, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER))
    }

    private fun toggleOverlay() {
        val fabParams = fabContainer.layoutParams as WindowManager.LayoutParams
        windowManager.removeView(fabContainer)

        if (overlayShown) {
            overlay.clearScan()
            windowManager.removeView(overlay)
        } else {
            windowManager.addView(overlay, overlay.layoutParams())
            overlay.scanAllElements()
        }
        overlayShown = !overlayShown
        windowManager.addView(fabContainer, fabParams)
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
                        updateLabel()
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
    private fun updateLabel() {
        val label = fabContainer.getChildAt(1) as TextView
        val config = configProvider.getConfig()
        label.text = "${config.unitMode.name.lowercase()} (${config.densityString})"
    }
}
