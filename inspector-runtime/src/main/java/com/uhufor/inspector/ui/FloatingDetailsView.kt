package com.uhufor.inspector.ui

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.getSystemService
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.uhufor.inspector.UnitMode
import com.uhufor.inspector.engine.SelectionState
import com.uhufor.inspector.engine.ViewMutator
import com.uhufor.inspector.ui.compose.LocalDetailsViewUiScale
import com.uhufor.inspector.util.dp
import com.uhufor.inspector.util.getScreenSize
import java.lang.ref.WeakReference
import kotlin.math.roundToInt

internal class FloatingDetailsView(context: Context) : LifecycleOwner, SavedStateRegistryOwner {
    private val context: WeakReference<Context> = WeakReference(context)
    private val windowManager: WeakReference<WindowManager> =
        WeakReference(context.getSystemService())

    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var isInstalled = false
    private val gapPx: Int = GAP_STICKY.dp().roundToInt()
    private val paddingHorizontalPx: Int = PADDING_HORIZONTAL.dp().roundToInt()
    private val paddingBottomPx: Int = PADDING_BOTTOM.dp().roundToInt()

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    private var onRefresh: (() -> Unit)? = null

    fun setOnRefresh(callback: (() -> Unit)?) {
        onRefresh = callback
    }

    fun install(selectionState: SelectionState, unitMode: UnitMode, uiScale: Float) {
        val currentContext = context.get() ?: return
        val currentWindowManager = windowManager.get() ?: return

        if (isInstalled) {
            composeView?.setContent {
                var editMode by remember { mutableStateOf(false) }
                LaunchedEffect(selectionState.id) {
                    editMode = false
                    setFocusable(false)
                }
                CompositionLocalProvider(LocalDetailsViewUiScale provides uiScale) {
                    ElementDetails(
                        selectionState = selectionState,
                        unitMode = unitMode,
                        isEditMode = editMode,
                        onEditModeChange = { editMode = it },
                        onRequestFocusable = { this@FloatingDetailsView.setFocusable(it) },
                        onApplyMarginPadding = { ml, mt, mr, mb, pl, pt, pr, pb ->
                            ViewMutator.setMarginById(selectionState.id, ml, mt, mr, mb)
                            ViewMutator.setPaddingById(selectionState.id, pl, pt, pr, pb)
                            ViewMutator.runAfterNextLayout(selectionState.id) { onRefresh?.invoke() }
                        }
                    )
                }
            }
            return
        }

        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        composeView = ComposeView(currentContext).apply {
            setViewTreeLifecycleOwner(this@FloatingDetailsView)
            setViewTreeSavedStateRegistryOwner(this@FloatingDetailsView)
            setContent {
                var editMode by remember { mutableStateOf(false) }
                LaunchedEffect(selectionState.id) {
                    editMode = false
                    setFocusable(false)
                }
                CompositionLocalProvider(LocalDetailsViewUiScale provides uiScale) {
                    ElementDetails(
                        selectionState = selectionState,
                        unitMode = unitMode,
                        isEditMode = editMode,
                        onEditModeChange = { editMode = it },
                        onRequestFocusable = { this@FloatingDetailsView.setFocusable(it) },
                        onApplyMarginPadding = { ml, mt, mr, mb, pl, pt, pr, pb ->
                            ViewMutator.setMarginById(selectionState.id, ml, mt, mr, mb)
                            ViewMutator.setPaddingById(selectionState.id, pl, pt, pr, pb)
                            ViewMutator.runAfterNextLayout(selectionState.id) { onRefresh?.invoke() }
                        }
                    )
                }
            }
            isVisible = false
            setPadding(
                paddingHorizontalPx,
                0,
                paddingHorizontalPx,
                paddingBottomPx,
            )
        }

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }

        runCatching {
            currentWindowManager.addView(composeView, layoutParams)
            isInstalled = true
        }.onFailure {
            isInstalled = false
        }
    }

    fun updateSticky(anchorRect: Rect) {
        if (!isInstalled) return
        val view = composeView ?: return
        val lp = layoutParams ?: return
        val wm = windowManager.get() ?: return

        val w = view.width
        val h = view.height
        if (w == 0 || h == 0) {
            view.post { updateSticky(anchorRect) }
            return
        }
        val left = anchorRect.left
        val top = anchorRect.top
        val right = anchorRect.right
        val bottom = anchorRect.bottom + paddingBottomPx

        val (sw, sh) = wm.getScreenSize()

        val isLeft = (left + (right - left) / 2) < (sw / 2)
        val isTop = (top + (bottom - top) / 2) < (sh / 2)

        val targetX = if (isLeft) right + gapPx else left - gapPx - w
        val targetY = if (isTop) top else bottom - h

        val clampedX = targetX.coerceIn(0, sw - w)
        val clampedY = targetY.coerceIn(0, sh - h)

        lp.gravity = Gravity.TOP or Gravity.START
        if (lp.x != clampedX || lp.y != clampedY) {
            lp.x = clampedX
            lp.y = clampedY
            runCatching { wm.updateViewLayout(view, lp) }
        }

        if (!view.isVisible) {
            view.isVisible = true
        }
    }

    fun uninstall() {
        if (!isInstalled) return
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        composeView?.let {
            it.disposeComposition()
            runCatching {
                windowManager.get()?.removeView(it)
            }
        }
        composeView = null
        layoutParams = null
        isInstalled = false
    }

    fun setFocusable(focusable: Boolean) {
        if (!isInstalled) return
        val view = composeView ?: return
        val lp = layoutParams ?: return
        val wm = windowManager.get() ?: return

        view.isFocusableInTouchMode = focusable
        view.isFocusable = focusable

        val prev = lp.flags
        var flags = prev
        if (focusable) {
            flags = flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            flags = flags and WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM.inv()
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        } else {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            flags = flags or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        }
        if (flags != prev) {
            lp.flags = flags
        }
        runCatching { wm.updateViewLayout(view, lp) }

        if (focusable) {
            view.requestFocus()
        }
    }

    companion object {
        private const val GAP_STICKY = 2
        private const val PADDING_BOTTOM = 8
        private const val PADDING_HORIZONTAL = 4
    }
}
