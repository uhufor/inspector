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
import com.uhufor.inspector.util.dp
import kotlin.random.Random
import androidx.core.graphics.toColorInt

@SuppressLint("ClickableViewAccessibility")
internal class OverlayCanvas(
    app: Application,
    private val cfg: Config,
) : View(app) {

    private val normalBorderWidth = 1.dp(context).toFloat()
    private val clickableBorderWidth = 2.dp(context).toFloat()
    
    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = normalBorderWidth
        style = Paint.Style.STROKE
        color = Color.RED
    }
    private val paintClickableBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = clickableBorderWidth
        style = Paint.Style.STROKE
        color = Color.RED
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        color = Color.RED
    }
    
    private val elementColors = listOf(
        "#F44336".toColorInt(), // Red
        "#E91E63".toColorInt(), // Pink
        "#9C27B0".toColorInt(), // Purple
        "#673AB7".toColorInt(), // Deep Purple
        "#3F51B5".toColorInt(), // Indigo
        "#2196F3".toColorInt(), // Blue
        "#03A9F4".toColorInt(), // Light Blue
        "#00BCD4".toColorInt(), // Cyan
        "#009688".toColorInt(), // Teal
        "#4CAF50".toColorInt(), // Green
        "#8BC34A".toColorInt(), // Light Green
        "#CDDC39".toColorInt(), // Lime
        "#FFEB3B".toColorInt(), // Yellow
        "#FFC107".toColorInt(), // Amber
        "#FF9800".toColorInt(), // Orange
        "#FF5722".toColorInt(), // Deep Orange
        "#795548".toColorInt(), // Brown
        "#9E9E9E".toColorInt(), // Grey
        "#607D8B".toColorInt(), // Blue Grey
        "#000000".toColorInt()  // Black
    )
    
    private val elementColorMap = mutableMapOf<Int, Int>()

    val engine = InspectorEngine(app) { postInvalidate() }

    init {
        setBackgroundColor(Color.TRANSPARENT)
        setOnTouchListener { _, ev ->
            if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
                engine.handleTap(ev.rawX, ev.rawY)
                true
            } else false
        }
    }

    private fun getColorForElement(element: Any): Int {
        val hashCode = element.hashCode()
        return elementColorMap.getOrPut(hashCode) {
            elementColors[Random.nextInt(elementColors.size)]
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        engine.allElements.forEach { element ->
            val color = getColorForElement(element)
            
            if (element.isClickable) {
                paintClickableBorder.color = color
                canvas.drawRect(element.bounds, paintClickableBorder)
            } else {
                paintBorder.color = color
                canvas.drawRect(element.bounds, paintBorder)
            }
        }
        
        val sel = engine.selection ?: return
        
        val selectedPaint = if (sel.isClickable) paintClickableBorder else paintBorder
        selectedPaint.color = Color.WHITE
        canvas.drawRect(sel.bounds, selectedPaint)
        selectedPaint.color = Color.RED
        
        val dm = resources.displayMetrics
        val w = UnitConverter.format(sel.bounds.width(), dm, cfg.unitMode)
        val h = UnitConverter.format(sel.bounds.height(), dm, cfg.unitMode)
        
        paintText.color = Color.WHITE
        canvas.drawText("$w × $h", sel.bounds.left, sel.bounds.top - 8, paintText)
        paintText.color = Color.RED
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
