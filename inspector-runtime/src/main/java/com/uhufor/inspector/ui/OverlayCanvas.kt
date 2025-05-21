package com.uhufor.inspector.ui

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.uhufor.inspector.Config
import com.uhufor.inspector.engine.InspectorEngine
import com.uhufor.inspector.util.UnitConverter

@SuppressLint("ClickableViewAccessibility")
internal class OverlayCanvas(
    app: Application,
    private val cfg: Config,
) : View(app) {

    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
        color = Color.RED
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        color = Color.RED
    }

    private val engine = InspectorEngine(app) { postInvalidate() }

    init {
        setBackgroundColor(Color.TRANSPARENT)
        setOnTouchListener { _, ev ->
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                engine.handleTap(ev.rawX, ev.rawY)
                true
            } else false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val sel = engine.selection ?: return
        canvas.drawRect(sel.bounds, paintBorder)

        val dm = resources.displayMetrics
        val w = UnitConverter.format(sel.bounds.width(), dm, cfg.unitMode)
        val h = UnitConverter.format(sel.bounds.height(), dm, cfg.unitMode)
        canvas.drawText("$w × $h", sel.bounds.left, sel.bounds.top - 8, paintText)
    }

    fun layoutParams(): WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.START or Gravity.TOP }
}
