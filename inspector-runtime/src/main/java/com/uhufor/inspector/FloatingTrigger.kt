package com.uhufor.inspector

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uhufor.inspector.ui.OverlayCanvas
import com.uhufor.inspector.ui.OverlayCanvas.BackKeyListener
import com.uhufor.inspector.util.dp

@SuppressLint("StaticFieldLeak")
internal object FloatingTrigger {
    private lateinit var windowManager: WindowManager
    private lateinit var overlay: OverlayCanvas
    private lateinit var configProvider: ConfigProvider
    private lateinit var fabContainer: View
    private var overlayShown = false

    fun install(context: Context) {
        if (!Settings.canDrawOverlays(context)) {
            context.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            return
        }

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

    private fun buildFab(context: Context): View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        val themedCtx = ContextThemeWrapper(
            context,
            androidx.appcompat.R.style.Theme_AppCompat_Light_NoActionBar
        )
        val fab = FloatingActionButton(themedCtx).apply {
            setImageResource(android.R.drawable.ic_menu_search)
            setOnClickListener { toggleOverlay() }
            setOnLongClickListener {
                showMenu(this)
                true
            }
        }
        val label = TextView(themedCtx).apply {
            id = View.generateViewId()
            textSize = 10f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.RED)
        }
        addView(fab, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        addView(label, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, Gravity.CENTER))
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
            menu.add(0, 1, 0, "Switch to ${if (config.unitMode == UnitMode.DP) "px" else "dp"}")
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
        val label = (fabContainer as ViewGroup).getChildAt(1) as TextView
        val config = configProvider.getConfig()
        label.text = "${config.unitMode.name.lowercase()} (${config.densityString})"
    }
}
