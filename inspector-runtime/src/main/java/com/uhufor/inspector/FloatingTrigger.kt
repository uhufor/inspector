package com.uhufor.inspector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.util.Size
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import com.uhufor.inspector.ui.TriggerLayout
import com.uhufor.inspector.util.AnchorView
import com.uhufor.inspector.util.FloatingViewDragHelper
import com.uhufor.inspector.util.FloatingViewDragHelperDelegate
import com.uhufor.inspector.util.ScreenSizeProvider
import com.uhufor.inspector.util.dp
import com.uhufor.inspector.util.getScreenSize
import java.lang.ref.WeakReference

internal class FloatingTrigger(
    context: Context,
    private val inspector: Inspector,
    private val positionRectChangeListener: AnchorView.OnPositionRectChangeListener,
) {
    private val contextRef: WeakReference<Context> = WeakReference(context)
    private val windowManager: WeakReference<WindowManager> =
        WeakReference(context.getSystemService())

    private var triggerLayout: TriggerLayout? = null
    private var triggerLayoutParams: WindowManager.LayoutParams? = null
    private var dragHelper: FloatingViewDragHelper? = null

    private val onLayoutChangeListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        clampToBounds()
    }

    private var isInstalled = false

    fun install() {
        if (isInstalled) return
        val currentContext = contextRef.get() ?: return
        val currentWindowManager = windowManager.get() ?: return

        val triggerLayout = createTriggerLayout(currentContext)
        val triggerLayoutParams = createLayoutParams()
        updateTriggerLayoutEnableState(triggerLayout)
        setupButtonClickListener(triggerLayout)

        val currentDragHelper = buildDragHelper(currentWindowManager, triggerLayoutParams).also {
            dragHelper = it
        }
        triggerLayout.setFloatingViewDragHelper(currentDragHelper)

        runCatching {
            currentWindowManager.addView(triggerLayout, triggerLayoutParams)
            positionAndShow(triggerLayout, currentWindowManager, triggerLayoutParams)
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
        val lp = triggerLayoutParams ?: return null
        val view = triggerLayout ?: return null
        val w = view.width
        val h = view.height
        if (w == 0 || h == 0) return null

        return Rect(lp.x, lp.y, lp.x + w, lp.y + h)
    }

    private fun notifyAnchorChanged() {
        val anchorRect = buildAnchorRect() ?: return
        positionRectChangeListener.onPositionRectChange(anchorRect)
    }

    fun requestUpdateAnchor() {
        notifyAnchorChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun uninstall() {
        if (!isInstalled) return

        runCatching {
            windowManager.get()?.removeView(triggerLayout)
        }
        triggerLayout?.removeOnLayoutChangeListener(onLayoutChangeListener)
        triggerLayout = null
        triggerLayoutParams = null
        dragHelper = null
        isInstalled = false
    }

    fun bringToFront() {
        if (!isInstalled || triggerLayout == null || triggerLayoutParams == null) return

        runCatching {
            windowManager.get()?.removeView(triggerLayout)
            windowManager.get()?.addView(triggerLayout, triggerLayoutParams)
        }
    }

    fun refreshEnableState() {
        triggerLayout?.let(::updateTriggerLayoutEnableState)
    }

    private fun clampToBounds() {
        val lp = triggerLayoutParams ?: return
        val view = triggerLayout ?: return
        val wm = windowManager.get() ?: return

        val screen = wm.getScreenSize()
        val w = view.width
        val h = view.height
        if (w == 0 || h == 0) return

        val hMarginPx = HORIZONTAL_MARGIN.dp().toInt()
        val maxX = (screen.width - w - hMarginPx).coerceAtLeast(hMarginPx)
        val maxY = (screen.height - h).coerceAtLeast(0)

        val newX = lp.x.coerceIn(hMarginPx, maxX)
        val newY = lp.y.coerceIn(0, maxY)

        if (newX != lp.x || newY != lp.y) {
            runCatching { applyAndUpdatePosition(wm, newX, newY) }
        }
    }

    private fun createTriggerLayout(ctx: Context): TriggerLayout {
        return TriggerLayout(ctx).also {
            this.triggerLayout = it
            it.isVisible = false
        }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
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
        wm: WindowManager,
        lp: WindowManager.LayoutParams,
    ): FloatingViewDragHelper {
        return FloatingViewDragHelper(
            screenSizeProvider = object : ScreenSizeProvider {
                override fun getSize(): Size {
                    return windowManager.get()?.getScreenSize() ?: Size(0, 0)
                }
            },
            delegate = object : FloatingViewDragHelperDelegate {
                override fun getPosition(): Pair<Int, Int> {
                    return Pair(lp.x, lp.y)
                }

                override fun getSize(): Size {
                    return Size(
                        this@FloatingTrigger.triggerLayout?.width ?: 0,
                        this@FloatingTrigger.triggerLayout?.height ?: 0
                    )
                }

                override fun onChangePosition(x: Int, y: Int) {
                    applyAndUpdatePosition(wm, x, y)
                }
            },
            horizontalMargin = HORIZONTAL_MARGIN
        )
    }

    private fun positionAndShow(
        layout: TriggerLayout,
        wm: WindowManager,
        lp: WindowManager.LayoutParams,
    ) {
        layout.post {
            val screen = wm.getScreenSize()
            val w = layout.width
            val h = layout.height
            val initialX = w / 2
            val initialY = ((screen.height - h) / 2).coerceIn(0, screen.height - h)

            lp.x = initialX
            lp.y = initialY
            wm.updateViewLayout(layout, lp)

            layout.isVisible = true
            layout.addOnLayoutChangeListener(onLayoutChangeListener)

            notifyAnchorChanged()
        }
    }

    private fun applyAndUpdatePosition(wm: WindowManager, x: Int, y: Int) {
        val lp = triggerLayoutParams ?: return
        val view = triggerLayout ?: return
        lp.x = x
        lp.y = y
        wm.updateViewLayout(view, lp)
        notifyAnchorChanged()
    }

    companion object {
        private const val HORIZONTAL_MARGIN = 10
    }
}
