package com.uhufor.inspector.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import com.uhufor.inspector.Config
import com.uhufor.inspector.ConfigProvider
import com.uhufor.inspector.engine.InspectorEngine
import com.uhufor.inspector.engine.MeasurementMode
import com.uhufor.inspector.engine.SelectionState
import com.uhufor.inspector.util.SwipeGestureDetector
import com.uhufor.inspector.util.UnitConverter
import com.uhufor.inspector.util.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private fun Paint.withColor(color: Int, block: (Paint) -> Unit) {
    val originalColor = this.color
    try {
        this.color = color
        block(this)
    } finally {
        this.color = originalColor
    }
}

private fun Paint.withBorderWidth(width: Float, block: (Paint) -> Unit) {
    val originalWidth = this.strokeWidth
    try {
        this.strokeWidth = width
        block(this)
    } finally {
        this.strokeWidth = originalWidth
    }
}

@SuppressLint("ClickableViewAccessibility")
internal class OverlayCanvas @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    interface BackKeyListener {
        fun onBackPressed()
    }

    var backKeyListener: BackKeyListener? = null

    private var internalConfigProvider: ConfigProvider? = null
    private var internalEngine: InspectorEngine? = null

    private val displayMetrics = context.resources.displayMetrics

    private val cfg: Config
        get() = internalConfigProvider
            ?.getConfig()
            ?: throw IllegalStateException("ConfigProvider must be set before accessing config.")

    private val thinBorderWidth = 1.dp()
    private val thickBorderWidth = 2.dp()

    private val paintBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.CYAN
    }

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.CYAN
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = TEXT_SIZE.dp()
        color = Color.RED
    }

    private val paintDistanceText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = DISTANCE_TEXT_SIZE.dp()
        color = Color.WHITE
    }

    private val paintDistanceLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = DASHED_LINE_WIDTH.dp()
        style = Paint.Style.STROKE
        color = Color.WHITE
        pathEffect = android.graphics.DashPathEffect(
            floatArrayOf(DASH_PATTERN_ON, DASH_PATTERN_OFF),
            DASH_PHASE
        )
    }

    private val paintPorterDuffClear = Paint().apply {
        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
    }

    private val elementColorMap = mutableMapOf<Int, Int>()

    private val gestureDetector = GestureDetector(
        context,
        object : SwipeGestureDetector() {
            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                internalEngine?.handleTap(e.rawX, e.rawY)
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                internalEngine?.handleLongPress(e.rawX, e.rawY)
            }

            override fun onSwipe(direction: GestureDirection) {
                internalEngine?.handleSwipe(direction)
            }
        }
    )

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setBackgroundColor(Color.TRANSPARENT)
        setOnTouchListener { _, ev ->
            gestureDetector.onTouchEvent(ev)
        }
    }

    fun setConfigProvider(provider: ConfigProvider) {
        this.internalConfigProvider = provider
    }

    fun setEngine(engine: InspectorEngine) {
        this.internalEngine = engine
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

        when (internalEngine?.measurementMode) {
            MeasurementMode.Normal -> drawSelectedElement(canvas)
            MeasurementMode.Relative -> drawRelativeMeasurement(canvas)
            else -> Unit
        }
    }

    private fun drawAllElements(canvas: Canvas) {
        internalEngine?.allElements?.forEach { element ->
            val borderColor = applyAlpha(getColorForElement(element), 0.5f)
            drawElementBorder(canvas, element.bounds, element.isClickable, borderColor)
        }
    }

    private fun drawElementInfo(
        canvas: Canvas,
        selection: SelectionState,
        elementBaseColor: Int,
    ) {
        val complementaryColor = getComplementaryColor(elementBaseColor)
        drawElementBorder(canvas, selection.bounds, selection.isClickable, complementaryColor)
        drawElementSizeInfo(canvas, selection.bounds, elementBaseColor, complementaryColor)
    }

    private fun drawElementBorder(
        canvas: Canvas,
        bounds: RectF,
        useThickBorder: Boolean,
        borderColor: Int,
    ) {
        val borderWidth = if (useThickBorder) thickBorderWidth else thinBorderWidth
        paintBorder.withBorderWidth(borderWidth) { paintBorder ->
            paintBorder.withColor(borderColor) { paintColor ->
                canvas.drawRect(bounds, paintColor)
            }
        }
    }

    private fun drawElementSizeInfo(
        canvas: Canvas,
        bounds: RectF,
        textColor: Int,
        textBackgroundColor: Int,
    ) {
        val sizeText = getElementSizeText(bounds)
        val textWidth = paintText.measureText(sizeText)
        var textDrawX = bounds.left
        val textDrawYBaseline = bounds.top - DIMENSION_TEXT_OFFSET.dp()

        val fm = paintText.fontMetrics
        var bgLeft = textDrawX
        val bgTopInitial = textDrawYBaseline + fm.top
        var bgRight = textDrawX + textWidth
        var finalBgTop = bgTopInitial
        var finalTextDrawYBaseline = textDrawYBaseline

        if (bgLeft < 0f) {
            textDrawX = 0f
            bgLeft = 0f
            bgRight = textWidth
        } else if (bgLeft + textWidth > getWidth()) {
            textDrawX = getWidth() - textWidth
            bgLeft = textDrawX
            bgRight = getWidth().toFloat()
        }

        if (bgTopInitial < 0f) {
            val vShift = -bgTopInitial
            finalBgTop = 0f
            finalTextDrawYBaseline += vShift
        }
        val finalBgBottom = finalTextDrawYBaseline + fm.bottom

        paintBackground.withColor(textBackgroundColor) { paint ->
            canvas.drawRect(bgLeft, finalBgTop, bgRight, finalBgBottom, paint)
        }
        paintText.withColor(textColor) { paint ->
            canvas.drawText(sizeText, textDrawX, finalTextDrawYBaseline, paint)
        }
    }

    private fun getElementSizeText(bounds: RectF): String {
        val widthText = UnitConverter.format(bounds.width(), displayMetrics, cfg.unitMode)
        val heightText =
            UnitConverter.format(bounds.height(), displayMetrics, cfg.unitMode)
        return "$widthText x $heightText"
    }

    private fun drawSelectedElement(canvas: Canvas) {
        val currentEngine = internalEngine ?: return
        val selection = currentEngine.selection ?: return

        selection.parentBounds?.let { parentBounds ->
            drawDarkBackground(canvas, selection.bounds, parentBounds)
        }

        val elementColor = getColorForElement(selection)
        drawElementInfo(canvas, selection, elementColor)

        selection.parentBounds?.let { parentBounds ->
            drawDistanceToBounds(canvas, selection.bounds, parentBounds)
        }
    }

    private fun drawDarkBackground(
        canvas: Canvas,
        childBounds: RectF,
        parentBounds: RectF,
    ) {
        val path = android.graphics.Path().apply {
            addRect(parentBounds, android.graphics.Path.Direction.CW)
            addRect(childBounds, android.graphics.Path.Direction.CCW)
        }
        paintBackground.withColor(BG_COLOR_DARK.toColorInt()) { paintColor ->
            canvas.drawPath(path, paintColor)
        }
    }

    private fun drawDistanceToBounds(
        canvas: Canvas,
        childBounds: RectF,
        parentBounds: RectF,
    ) {
        val leftDistance = childBounds.left - parentBounds.left
        if (leftDistance > 0) {
            val distanceText = UnitConverter.format(leftDistance, displayMetrics, cfg.unitMode)
            drawDistanceLine(
                canvas,
                parentBounds.left, childBounds.top + childBounds.height() / 2,
                childBounds.left, childBounds.top + childBounds.height() / 2,
                distanceText
            )
        }

        val rightDistance = parentBounds.right - childBounds.right
        if (rightDistance > 0) {
            val distanceText = UnitConverter.format(rightDistance, displayMetrics, cfg.unitMode)
            drawDistanceLine(
                canvas,
                childBounds.right, childBounds.top + childBounds.height() / 2,
                parentBounds.right, childBounds.top + childBounds.height() / 2,
                distanceText
            )
        }

        val topDistance = childBounds.top - parentBounds.top
        if (topDistance > 0) {
            val distanceText = UnitConverter.format(topDistance, displayMetrics, cfg.unitMode)
            drawDistanceLine(
                canvas,
                childBounds.left + childBounds.width() / 2, parentBounds.top,
                childBounds.left + childBounds.width() / 2, childBounds.top,
                distanceText
            )
        }

        val bottomDistance = parentBounds.bottom - childBounds.bottom
        if (bottomDistance > 0) {
            val distanceText = UnitConverter.format(bottomDistance, displayMetrics, cfg.unitMode)
            drawDistanceLine(
                canvas,
                childBounds.left + childBounds.width() / 2, childBounds.bottom,
                childBounds.left + childBounds.width() / 2, parentBounds.bottom,
                distanceText
            )
        }
    }

    private fun drawDistanceLine(
        canvas: Canvas,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        distanceText: String,
    ) {
        canvas.drawLine(startX, startY, endX, endY, paintDistanceLine)
        drawArrow(canvas, startX, startY, endX, endY, paintDistanceLine)
        drawArrow(canvas, endX, endY, startX, startY, paintDistanceLine)

        val textWidth = paintDistanceText.measureText(distanceText)
        val textX = (startX + endX) / 2 - textWidth / 2
        val textY = (startY + endY) / 2 + TEXT_VERTICAL_OFFSET

        val textBgRect = RectF(
            textX - TEXT_PADDING_HORIZONTAL.dp(),
            textY - TEXT_PADDING_TOP.dp(),
            textX + textWidth + TEXT_PADDING_HORIZONTAL.dp(),
            textY + TEXT_PADDING_BOTTOM.dp()
        )

        paintBackground.withColor(BG_COLOR_DEEP_DARK.toColorInt()) { paintColor ->
            canvas.drawRect(textBgRect, paintColor)
        }
        canvas.drawText(distanceText, textX, textY, paintDistanceText)
    }

    private fun drawArrow(
        canvas: Canvas,
        fromX: Float,
        fromY: Float,
        toX: Float,
        toY: Float,
        paint: Paint,
    ) {
        val angle = atan2((toY - fromY).toDouble(), (toX - fromX).toDouble())
        val arrowSize = ARROW_SIZE.dp()

        val arrowX1 = fromX + arrowSize * cos(angle - ARROW_ANGLE).toFloat()
        val arrowY1 = fromY + arrowSize * sin(angle - ARROW_ANGLE).toFloat()
        val arrowX2 = fromX + arrowSize * cos(angle + ARROW_ANGLE).toFloat()
        val arrowY2 = fromY + arrowSize * sin(angle + ARROW_ANGLE).toFloat()

        canvas.drawLine(fromX, fromY, arrowX1, arrowY1, paint)
        canvas.drawLine(fromX, fromY, arrowX2, arrowY2, paint)
    }

    private fun drawRelativeMeasurement(canvas: Canvas) {
        val currentEngine = internalEngine ?: return

        val primary = currentEngine.primarySelection ?: return
        val primaryElementColor = getColorForElement(primary)

        val secondary = currentEngine.secondarySelection
        if (secondary == null) {
            // have primary only
            paintBackground.withColor(BG_COLOR_RED.toColorInt()) { paintColor ->
                canvas.drawRect(primary.bounds, paintColor)
            }
            drawElementInfo(canvas, primary, primaryElementColor)
        } else {
            // have primary and secondary both
            paintBackground.withColor(BG_COLOR_DARK.toColorInt()) { paintColor ->
                canvas.drawRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), paintColor)
            }

            canvas.drawRect(primary.bounds, paintPorterDuffClear)
            canvas.drawRect(secondary.bounds, paintPorterDuffClear)

            paintBackground.withColor(BG_COLOR_RED.toColorInt()) { paintColor ->
                canvas.drawRect(primary.bounds, paintColor)
            }
            drawElementInfo(canvas, primary, primaryElementColor)

            val secondaryElementColor = getColorForElement(secondary)
            paintBackground.withColor(BG_COLOR_BLUE.toColorInt()) { paintColor ->
                canvas.drawRect(secondary.bounds, paintColor)
            }
            drawElementInfo(canvas, secondary, secondaryElementColor)

            drawRelativeDistances(canvas, displayMetrics)
        }
    }

    private fun drawRelativeDistances(canvas: Canvas, dm: android.util.DisplayMetrics) {
        val distances = internalEngine?.getRelativeDistances() ?: return
        if (distances.isEmpty()) return

        distances.forEach { distance ->
            drawDistanceLine(
                canvas,
                distance.startX,
                distance.startY,
                distance.endX,
                distance.endY,
                UnitConverter.format(distance.value, dm, cfg.unitMode)
            )
        }
    }

    private fun getColorForElement(element: Any) =
        elementColorMap.getOrPut(element.hashCode()) {
            ELEMENT_COLORS[Random.nextInt(ELEMENT_COLORS.size)]
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

    fun getSelection() = internalEngine?.selection

    fun getPrimarySelection() = internalEngine?.primarySelection

    fun getSecondarySelection() = internalEngine?.secondarySelection

    fun getMeasurementMode() = internalEngine?.measurementMode

    fun getAllElements() = internalEngine?.allElements

    companion object {
        private const val TEXT_SIZE = 8f
        private const val DISTANCE_TEXT_SIZE = 7f
        private const val DASHED_LINE_WIDTH = 1.2f
        private const val DASH_PATTERN_ON = 10f
        private const val DASH_PATTERN_OFF = 5f
        private const val DASH_PHASE = 0f
        private const val TEXT_PADDING_HORIZONTAL = 1f
        private const val TEXT_PADDING_TOP = 7f
        private const val TEXT_PADDING_BOTTOM = 2f
        private const val TEXT_VERTICAL_OFFSET = DISTANCE_TEXT_SIZE
        private const val ARROW_SIZE = 4f
        private const val ARROW_ANGLE = Math.PI / 6
        private const val BG_COLOR_DEEP_DARK = "#AC000000"
        private const val BG_COLOR_DARK = "#50000000"
        private const val BG_COLOR_RED = "#60FFAAAA"
        private const val BG_COLOR_BLUE = "#60AAAAFF"
        private const val DIMENSION_TEXT_OFFSET = 3f

        private val ELEMENT_COLORS = listOf(
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
        )
    }
}
