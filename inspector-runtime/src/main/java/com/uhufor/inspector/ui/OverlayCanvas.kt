package com.uhufor.inspector.ui

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.toColorInt
import com.uhufor.inspector.Config
import com.uhufor.inspector.engine.InspectorEngine
import com.uhufor.inspector.util.UnitConverter
import com.uhufor.inspector.util.dp
import kotlin.random.Random

@SuppressLint("ClickableViewAccessibility")
internal class OverlayCanvas(
    app: Application,
    private val cfg: Config,
) : View(app) {

    interface BackKeyListener {
        fun onBackPressed()
    }

    var backKeyListener: BackKeyListener? = null

    private val normalBorderWidth = 1.dp(context).toFloat()
    private val clickableBorderWidth = 2.dp(context).toFloat()
    private val elementColorMap = mutableMapOf<Int, Int>()

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
        "#F44336".toColorInt(),
        "#E91E63".toColorInt(),
        "#9C27B0".toColorInt(),
        "#673AB7".toColorInt(),
        "#3F51B5".toColorInt(),
        "#2196F3".toColorInt(),
        "#03A9F4".toColorInt(),
        "#00BCD4".toColorInt(),
        "#009688".toColorInt(),
        "#4CAF50".toColorInt(),
        "#8BC34A".toColorInt(),
        "#CDDC39".toColorInt(),
        "#FFEB3B".toColorInt(),
        "#FFC107".toColorInt(),
        "#FF9800".toColorInt(),
        "#FF5722".toColorInt(),
        "#795548".toColorInt(),
        "#9E9E9E".toColorInt(),
        "#607D8B".toColorInt(),
        "#000000".toColorInt()
    )

    val engine = InspectorEngine(app) { postInvalidate() }

    init {
        setBackgroundColor(Color.TRANSPARENT)
        isFocusable = true
        isFocusableInTouchMode = true
        setOnTouchListener(::handleTouch)
    }

    private fun handleTouch(v: View, event: MotionEvent): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            engine.handleTap(event.rawX, event.rawY)
            requestFocus()
            return true
        }
        return false
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backKeyListener?.onBackPressed()
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawAllElements(canvas)
        drawSelectedElement(canvas)
    }

    private fun drawAllElements(canvas: Canvas) {
        engine.allElements.forEach { element ->
            val color = applyAlpha(getColorForElement(element), 0.5f)
            val paint = if (element.isClickable) paintClickableBorder else paintBorder
            paint.color = color
            canvas.drawRect(element.bounds, paint)
            paint.color = Color.RED
        }
    }

    private fun drawSelectedElement(canvas: Canvas) {
        val selected = engine.selection ?: return
        val color = getComplementaryColor(getColorForElement(selected))
        val paint = if (selected.isClickable) paintClickableBorder else paintBorder

        paint.color = color
        canvas.drawRect(selected.bounds, paint)

        val dm = resources.displayMetrics
        val width = UnitConverter.format(selected.bounds.width(), dm, cfg.unitMode)
        val height = UnitConverter.format(selected.bounds.height(), dm, cfg.unitMode)

        paintText.color = color
        canvas.drawText(
            "$width × $height",
            selected.bounds.left,
            selected.bounds.top - 8,
            paintText
        )
        paintText.color = Color.RED
    }

    private fun getColorForElement(element: Any) =
        elementColorMap.getOrPut(element.hashCode()) {
            elementColors[Random.nextInt(elementColors.size)]
        }

    private fun getComplementaryColor(color: Int) = Color.rgb(
        255 - Color.red(color),
        255 - Color.green(color),
        255 - Color.blue(color)
    )

    private fun applyAlpha(color: Int, alpha: Float) = Color.argb(
        (Color.alpha(color) * alpha).toInt(),
        Color.red(color),
        Color.green(color),
        Color.blue(color)
    )

    fun layoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    ).apply { gravity = Gravity.START or Gravity.TOP }
}
