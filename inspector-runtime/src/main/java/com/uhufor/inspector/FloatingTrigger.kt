package com.uhufor.inspector

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uhufor.inspector.ui.OverlayCanvas
import com.uhufor.inspector.util.dp

@SuppressLint("InflateParams", "StaticFieldLeak")
internal object FloatingTrigger {

    private lateinit var wm: WindowManager
    private lateinit var overlay: OverlayCanvas
    private lateinit var cfg: Config
    private lateinit var fabContainer: View
    private var overlayShown = false

    fun install(app: Application, config: Config) {
        if (!Settings.canDrawOverlays(app)) {
            app.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${app.packageName}".toUri()
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            Log.i("FloatingTrigger", "Requesting overlay permission")
            return
        }
        wm = app.getSystemService()!!
        cfg = config

        overlay = OverlayCanvas(app, cfg)

        fabContainer = buildFab(app)

        val lp = WindowManager.LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.BOTTOM
            x = 16.dp(app)
            y = 96.dp(app)
        }
        wm.addView(fabContainer, lp)

        cfg.densityString = "%.2fx".format(app.resources.displayMetrics.density)
//        updateLabel()
    }

    @SuppressLint("StaticFieldLeak")
    private fun buildFab(app: Application): View = FrameLayout(app).apply {
        val themedCtx = ContextThemeWrapper(
            app,
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
        addView(label, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.CENTER))
    }

    private fun toggleOverlay() {
        if (overlayShown) wm.removeView(overlay)
        else wm.addView(overlay, overlay.layoutParams())
        overlayShown = !overlayShown
    }

    private fun showMenu(anchor: View) {
        PopupMenu(anchor.context, anchor).apply {
            menu.add(0, 1, 0, "Switch to ${if (cfg.unitMode == UnitMode.DP) "px" else "dp"}")
            menu.add(0, 2, 1, if (overlayShown) "Hide overlay" else "Show overlay")
            setOnMenuItemClickListener {
                when (it.itemId) {
                    1 -> {
                        cfg.unitMode = if (cfg.unitMode == UnitMode.DP) UnitMode.PX else UnitMode.DP
//                        updateLabel()
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
        label.text = "${cfg.unitMode.name.lowercase()} (${cfg.densityString})"
    }
}
