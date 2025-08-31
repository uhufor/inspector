package com.uhufor.inspector

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.annotation.MainThread
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import com.uhufor.inspector.ui.OverlayCanvas
import com.uhufor.inspector.ui.TriggerLayout
import com.uhufor.inspector.util.AnchorView
import com.uhufor.inspector.util.FloatingViewDragHelper
import com.uhufor.inspector.util.FloatingViewDragHelperDelegate
import com.uhufor.inspector.util.ScreenSizeProvider
import com.uhufor.inspector.util.dp
import com.uhufor.inspector.util.getScreenSize

internal class FloatingTrigger(
    context: Context,
    private val inspector: Inspector,
    private val positionRectChangeListener: AnchorView.OnPositionRectChangeListener,
) {
    private val appContext: Context = context.applicationContext
    private val windowManager: WindowManager = requireNotNull(appContext.getSystemService())

    private var triggerLayout: TriggerLayout? = null
    private var triggerLayoutParams: WindowManager.LayoutParams? = null
    private var dragHelper: FloatingViewDragHelper? = null
    private var backKeyListener: OverlayCanvas.BackKeyListener? = null

    private val onLayoutChangeListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        clampToBounds()
    }

    private var isInstalled = false

    fun setBackKeyListener(listener: OverlayCanvas.BackKeyListener?) {
        backKeyListener = listener
        triggerLayout?.setBackKeyListener(listener)
    }

    @MainThread
    fun install() {
        if (isInstalled) return

        val triggerLayout = createTriggerLayout(appContext)
        val triggerLayoutParams = createLayoutParams()
        updateTriggerLayoutEnableState(triggerLayout)
        setupButtonClickListener(triggerLayout)

        val currentDragHelper = buildDragHelper(windowManager, triggerLayoutParams).also {
            dragHelper = it
        }
        triggerLayout.setFloatingViewDragHelper(currentDragHelper)

        runCatching {
            this@FloatingTrigger.windowManager.addView(triggerLayout, triggerLayoutParams)
            positionAndShow(triggerLayout, this@FloatingTrigger.windowManager, triggerLayoutParams)
            isInstalled = true
        }.onFailure {
            isInstalled = false
        }
    }

    private fun updateTriggerLayoutEnableState(triggerLayout: TriggerLayout) {
        triggerLayout.setButtonEnableState(
            TriggerLayout.ButtonType.INSPECTION,
            inspector.isInspectionEnabled
        )
        triggerLayout.setButtonEnableState(
            TriggerLayout.ButtonType.DP,
            inspector.getUnitMode() == UnitMode.DP
        )
        triggerLayout.setButtonEnableState(
            TriggerLayout.ButtonType.SEE_PROPERTY_DETAILS,
            inspector.isDetailsViewEnabled
        )
    }

    private fun buildAnchorRect(): Rect? {
        val layoutParams = triggerLayoutParams ?: return null
        val view = triggerLayout ?: return null
        val width = view.width
        val height = view.height
        if (width == 0 || height == 0) return null

        return Rect(layoutParams.x, layoutParams.y, layoutParams.x + width, layoutParams.y + height)
    }

    private fun notifyAnchorChanged() {
        val anchorRect = buildAnchorRect() ?: return
        positionRectChangeListener.onPositionRectChange(anchorRect)
    }

    @MainThread
    fun requestUpdateAnchor() {
        notifyAnchorChanged()
    }

    @MainThread
    fun uninstall() {
        if (!isInstalled) return

        triggerLayout?.removeOnLayoutChangeListener(onLayoutChangeListener)
        runCatching {
            windowManager.removeView(triggerLayout)
        }
        triggerLayout = null
        triggerLayoutParams = null
        dragHelper = null
        isInstalled = false
    }

    @MainThread
    fun bringToFront() {
        if (!isInstalled || triggerLayout == null || triggerLayoutParams == null) return

        runCatching {
            windowManager.removeView(triggerLayout)
            windowManager.addView(triggerLayout, triggerLayoutParams)
        }
    }

    @MainThread
    fun refreshEnableState() {
        triggerLayout?.let(::updateTriggerLayoutEnableState)
    }

    private fun clampToBounds() {
        val layoutParams = triggerLayoutParams ?: return
        val view = triggerLayout ?: return

        val screen = windowManager.getScreenSize()
        val width = view.width
        val height = view.height
        if (width == 0 || height == 0) return

        val hMarginPx = horizontalMarginPx()
        val maxX = (screen.width - width - hMarginPx).coerceAtLeast(hMarginPx)
        val maxY = (screen.height - height).coerceAtLeast(0)

        val newX = layoutParams.x.coerceIn(hMarginPx, maxX)
        val newY = layoutParams.y.coerceIn(0, maxY)

        if (newX != layoutParams.x || newY != layoutParams.y) {
            runCatching { applyAndUpdatePosition(windowManager, newX, newY) }
        }
    }

    private fun createTriggerLayout(context: Context): TriggerLayout {
        return TriggerLayout(context).also {
            this.triggerLayout = it
            it.setBackKeyListener(backKeyListener)
            it.isVisible = false
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WINDOW_TYPE,
            WINDOW_FLAGS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = LAYOUT_GRAVITY
            x = 0
            y = 0
        }.also { this.triggerLayoutParams = it }
    }

    private fun setupButtonClickListener(layout: TriggerLayout) {
        layout.setOnButtonClickListener { buttonType ->
            when (buttonType) {
                TriggerLayout.ButtonType.INSPECTION -> inspector.toggleInspection()
                TriggerLayout.ButtonType.DP -> inspector.setUnitMode(
                    if (inspector.getUnitMode() == UnitMode.DP) UnitMode.PX else UnitMode.DP
                )

                TriggerLayout.ButtonType.SEE_PROPERTY_DETAILS -> inspector.enableDetailsView(
                    !inspector.isDetailsViewEnabled
                )
            }
            updateTriggerLayoutEnableState(layout)
        }
    }

    private fun buildDragHelper(
        windowManager: WindowManager,
        layoutParams: WindowManager.LayoutParams,
    ): FloatingViewDragHelper {
        return FloatingViewDragHelper(
            screenSizeProvider = object : ScreenSizeProvider {
                override fun getSize(): Size {
                    return windowManager.getScreenSize()
                }
            },
            delegate = object : FloatingViewDragHelperDelegate {
                override fun getPosition(): Pair<Int, Int> {
                    return Pair(layoutParams.x, layoutParams.y)
                }

                override fun getSize(): Size {
                    return Size(
                        this@FloatingTrigger.triggerLayout?.width ?: 0,
                        this@FloatingTrigger.triggerLayout?.height ?: 0
                    )
                }

                override fun onChangePosition(x: Int, y: Int) {
                    applyAndUpdatePosition(windowManager, x, y)
                }
            },
            horizontalMargin = HORIZONTAL_MARGIN
        )
    }

    private fun positionAndShow(
        layout: TriggerLayout,
        windowManager: WindowManager,
        layoutParams: WindowManager.LayoutParams,
    ) {
        layout.post {
            val screen = windowManager.getScreenSize()
            val (initialX, initialY) = computeInitialPosition(screen, layout.width, layout.height)
            layoutParams.x = initialX
            layoutParams.y = initialY
            updateViewLayoutSafely(windowManager, layout, layoutParams)

            layout.isVisible = true
            layout.addOnLayoutChangeListener(onLayoutChangeListener)

            notifyAnchorChanged()
        }
    }

    private fun applyAndUpdatePosition(windowManager: WindowManager, x: Int, y: Int) {
        val layoutParams = triggerLayoutParams ?: return
        val view = triggerLayout ?: return
        layoutParams.x = x
        layoutParams.y = y
        updateViewLayoutSafely(windowManager, view, layoutParams)
        notifyAnchorChanged()
    }

    private fun computeInitialPosition(
        screen: Size,
        viewWidth: Int,
        viewHeight: Int,
    ): Pair<Int, Int> {
        val x = viewWidth / 2
        val y = ((screen.height - viewHeight) / 2).coerceIn(0, screen.height - viewHeight)
        return x to y
    }

    private fun updateViewLayoutSafely(
        windowManager: WindowManager,
        view: View,
        layoutParams: WindowManager.LayoutParams,
    ) {
        runCatching { windowManager.updateViewLayout(view, layoutParams) }
    }

    private fun horizontalMarginPx(): Int = HORIZONTAL_MARGIN.dp().toInt()

    companion object {
        private const val HORIZONTAL_MARGIN = 10
        private const val WINDOW_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        private const val WINDOW_FLAGS = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        private const val LAYOUT_GRAVITY = Gravity.TOP or Gravity.START
    }
}
