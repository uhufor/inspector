package com.uhufor.inspector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.View
import android.util.Size
import android.view.Gravity
import android.view.WindowManager
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import com.uhufor.inspector.ui.TriggerLayout
import com.uhufor.inspector.util.AnchorView
import com.uhufor.inspector.util.FloatingViewDragHelper
import com.uhufor.inspector.util.FloatingViewDragHelperDelegate
import com.uhufor.inspector.util.ScreenSizeProvider
import com.uhufor.inspector.util.getScreenSize
import com.uhufor.inspector.util.dp
import java.lang.ref.WeakReference

internal class FloatingTrigger(
    context: Context,
    private val inspector: Inspector,
    private val positionRectChangeListener: AnchorView.OnPositionRectChangeListener,
) {
    private val context: WeakReference<Context> = WeakReference(context)
    private val windowManager: WeakReference<WindowManager> =
        WeakReference(context.getSystemService())

    private var triggerLayout: TriggerLayout? = null
    private var triggerLayoutParams: WindowManager.LayoutParams? = null
    private var dragHelper: FloatingViewDragHelper? = null

    private val onLayoutChangeListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
        clampToBounds()
    }

    private var isInstalled = false

    @SuppressLint("ClickableViewAccessibility")
    fun install() {
        if (isInstalled) return
        val currentContext = context.get() ?: return
        val currentWindowManager = windowManager.get() ?: return

        val triggerLayout = TriggerLayout(currentContext).also {
            this.triggerLayout = it
            it.isVisible = false
        }
        val triggerLayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }.also {
            this.triggerLayoutParams = it
        }
        updateTriggerLayoutEnableState(triggerLayout)
        triggerLayout.setOnButtonClickListener { buttonType ->
            when (buttonType) {
                TriggerLayout.ButtonType.INSPECTION -> {
                    inspector.toggleInspection()
                }

                TriggerLayout.ButtonType.DP -> {
                    inspector.setUnitMode(
                        if (inspector.getUnitMode() == UnitMode.DP) {
                            UnitMode.PX
                        } else {
                            UnitMode.DP
                        }
                    )
                }

                TriggerLayout.ButtonType.SEE_PROPERTY_DETAILS -> {
                    inspector.enableDetailsView(
                        !inspector.isDetailsViewEnabled
                    )
                }
            }
            updateTriggerLayoutEnableState(triggerLayout)
        }

        val currentDragHelper = FloatingViewDragHelper(
            screenSizeProvider = object : ScreenSizeProvider {
                override fun getSize(): Size {
                    return windowManager.get()?.getScreenSize() ?: Size(0, 0)
                }
            },
            delegate = object : FloatingViewDragHelperDelegate {
                override fun getPosition(): Pair<Int, Int> {
                    return Pair(triggerLayoutParams.x, triggerLayoutParams.y)
                }

                override fun getSize(): Size {
                    return Size(
                        this@FloatingTrigger.triggerLayout?.width ?: 0,
                        this@FloatingTrigger.triggerLayout?.height ?: 0
                    )
                }

                override fun onChangePosition(
                    x: Int,
                    y: Int,
                ) {
                    triggerLayoutParams.x = x
                    triggerLayoutParams.y = y
                    currentWindowManager.updateViewLayout(
                        this@FloatingTrigger.triggerLayout,
                        triggerLayoutParams
                    )
                    notifyAnchorChanged()
                }
            },
            horizontalMargin = HORIZONTAL_MARGIN
        ).also {
            dragHelper = it
        }
        triggerLayout.setFloatingViewDragHelper(currentDragHelper)

        runCatching {
            currentWindowManager.addView(triggerLayout, triggerLayoutParams)
            triggerLayout.post {
                val screen = currentWindowManager.getScreenSize()
                val w = triggerLayout.width
                val h = triggerLayout.height
                val initialX = w / 2
                val initialY = ((screen.height - h) / 2).coerceIn(0, screen.height - h)

                triggerLayoutParams.x = initialX
                triggerLayoutParams.y = initialY
                currentWindowManager.updateViewLayout(triggerLayout, triggerLayoutParams)

                triggerLayout.isVisible = true
                triggerLayout.addOnLayoutChangeListener(onLayoutChangeListener)

                notifyAnchorChanged()
            }
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
            lp.x = newX
            lp.y = newY
            runCatching { wm.updateViewLayout(view, lp) }
            notifyAnchorChanged()
        }
    }

    companion object {
        private const val HORIZONTAL_MARGIN = 10
    }
}
