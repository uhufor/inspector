package com.uhufor.inspector.engine

import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Checkable
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

internal object ViewHitTester {

    fun hitTest(root: View, x: Int, y: Int): SelectionState? {
        val hits = mutableListOf<View>()
        depthFirstSearch(root, x, y, hits)

        val view = hits.minByOrNull { it.width * it.height } ?: return null
        val rect = Rect()
        view.getGlobalVisibleRect(rect)

        val parent = if (view.parent is View) view.parent as View else null
        val parentRect = if (parent != null) {
            val parentBounds = Rect()
            parent.getGlobalVisibleRect(parentBounds)
            RectF(parentBounds)
        } else null

        return createSelectionState(view, rect, parentRect)
    }

    fun scanAllElements(root: View): List<SelectionState> {
        val elements = mutableListOf<SelectionState>()
        collectAllViews(root, elements)
        return elements
    }

    private fun depthFirstSearch(
        view: View,
        x: Int,
        y: Int,
        resultList: MutableList<View>,
    ) {
        if (!view.isShown) return

        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val (left, top) = location
        val right = left + view.width
        val bottom = top + view.height

        if (x < left || y < top || x > right || y > bottom) return

        resultList += view

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                depthFirstSearch(view.getChildAt(i), x, y, resultList)
            }
        }
    }

    private fun collectAllViews(view: View, elements: MutableList<SelectionState>) {
        if (!view.isShown || view.width == 0 || view.height == 0 || view.alpha == 0f) {
            return
        }

        val rect = Rect()
        if (view.getGlobalVisibleRect(rect)) {
            if (rect.width() > 0 && rect.height() > 0) {
                val parentRect = (view.parent as? View)?.run {
                    val parentBounds = Rect()
                    if (getGlobalVisibleRect(parentBounds) &&
                        parentBounds.width() > 0 &&
                        parentBounds.height() > 0
                    ) {
                        RectF(parentBounds)
                    } else {
                        null
                    }
                }

                elements.add(createSelectionState(view, rect, parentRect))
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectAllViews(view.getChildAt(i), elements)
            }
        }
    }

    private fun createSelectionState(
        view: View,
        rect: Rect,
        parentRect: RectF?,
    ): SelectionState {
        val state = SelectionState(
            id = view.hashCode(),
            bounds = RectF(rect),
            parentBounds = parentRect,
            properties = UiNodeProperties(
                type = UiNodeType.VIEW,
                id = getResourceId(view),
                size = Size(view.width, view.height),
                distance = view.getDistanceFromParent(),
                margin = view.getMargin(),
                padding = view.getPadding(),
                actions = buildSet {
                    if (view.isClickable) {
                        add(UiNodeActionProperties.CLICKABLE)
                    }
                    if (view.isLongClickable) {
                        add(UiNodeActionProperties.LONG_CLICKABLE)
                    }
                    if (view.isFocusable) {
                        add(UiNodeActionProperties.FOCUSABLE)
                    }
                    if (view is Checkable) {
                        add(UiNodeActionProperties.CHECKABLE)
                    }
                },
                styles = buildSet {
                    if (view is TextView) {
                        add(
                            UiNodeStyleProperties.TextStyle(
                                text = view.text.toString(),
                                textColor = view.currentTextColor,
                                textSize = view.textSize,
                                isBold = view.typeface?.isBold ?: false,
                                isItalic = view.typeface?.isItalic ?: false,
                            )
                        )
                    }
                    add(
                        UiNodeStyleProperties.ColorStyle(
                            backgroundType = view.background?.javaClass?.simpleName ?: "None",
                            backgroundColor = when (val drawable = view.background) {
                                is ColorDrawable -> {
                                    drawable.color
                                }

                                is RippleDrawable -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        drawable.effectColor.defaultColor
                                    } else {
                                        null
                                    }
                                }

                                is GradientDrawable -> {
                                    drawable.color?.defaultColor
                                }

                                else -> {
                                    null
                                }
                            }
                        )
                    )
                }
            ),
        )
        UiNodeViewRegistry.register(view)
        return state
    }

    private const val NO_ID = "NO_ID"
    private fun getResourceId(view: View): String {
        if (view.id == View.NO_ID) return NO_ID
        return runCatching {
            val packageName = when (view.id and 0xFF000000.toInt()) {
                0x7F000000 -> "app"
                0x01000000 -> "android"
                else -> view.resources.getResourcePackageName(view.id)
            }
            view.resources.run {
                "$packageName:${getResourceTypeName(view.id)}/${getResourceEntryName(view.id)}"
            }
        }.getOrDefault(NO_ID)
    }

    // TODO: change name to distance
    private fun View.getDistanceFromParent(): RectF {
        val parent = parent as? View ?: return RectF()

        val viewLocation = IntArray(2)
        val parentLocation = IntArray(2)

        getLocationOnScreen(viewLocation)
        parent.getLocationOnScreen(parentLocation)

        val viewLeft = viewLocation[0]
        val viewTop = viewLocation[1]
        val viewRight = viewLeft + width
        val viewBottom = viewTop + height

        val parentLeft = parentLocation[0]
        val parentTop = parentLocation[1]
        val parentRight = parentLeft + parent.width
        val parentBottom = parentTop + parent.height

        return RectF(
            (viewLeft - parentLeft).toFloat(),
            (viewTop - parentTop).toFloat(),
            (parentRight - viewRight).toFloat(),
            (parentBottom - viewBottom).toFloat()
        )
    }

    private fun View.getMargin(): RectF {
        return RectF(
            marginLeft.toFloat(),
            marginTop.toFloat(),
            marginRight.toFloat(),
            marginBottom.toFloat()
        )
    }

    private fun View.getPadding(): RectF {
        return RectF(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            paddingRight.toFloat(),
            paddingBottom.toFloat()
        )
    }
}

internal object UiNodeViewRegistry {
    private val views = ConcurrentHashMap<Int, WeakReference<View>>()

    fun register(view: View) {
        views[view.hashCode()] = WeakReference(view)
    }

    fun get(id: Int): View? = views[id]?.get()

    fun clear() {
        views.clear()
    }
}

internal object ViewMutator {
    fun setMarginById(id: Int, left: Int, top: Int, right: Int, bottom: Int) {
        val v = UiNodeViewRegistry.get(id) ?: return
        val lp = v.layoutParams
        if (lp is ViewGroup.MarginLayoutParams) {
            lp.setMargins(left, top, right, bottom)
            v.layoutParams = lp
            v.requestLayout()
            v.invalidate()
        }
    }

    fun setPaddingById(id: Int, left: Int, top: Int, right: Int, bottom: Int) {
        val v = UiNodeViewRegistry.get(id) ?: return
        v.setPadding(left, top, right, bottom)
        v.requestLayout()
        v.invalidate()
    }

    fun setTextById(id: Int, text: CharSequence) {
        val v = UiNodeViewRegistry.get(id) ?: return
        if (v is TextView) {
            v.text = text
            v.requestLayout()
            v.invalidate()
        }
    }

    fun setTextSizeSpById(id: Int, size: Float) {
        val v = UiNodeViewRegistry.get(id) ?: return
        if (v is TextView) {
            v.textSize = size
            v.requestLayout()
            v.invalidate()
        }
    }

    fun setTextColorById(id: Int, color: Int) {
        val v = UiNodeViewRegistry.get(id) ?: return
        if (v is TextView) {
            v.setTextColor(color)
            v.invalidate()
        }
    }

    fun runAfterNextLayout(id: Int, action: () -> Unit) {
        val v = UiNodeViewRegistry.get(id) ?: return
        val vto = v.viewTreeObserver
        val listener = object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (v.viewTreeObserver.isAlive) {
                    v.viewTreeObserver.removeOnPreDrawListener(this)
                }
                action()
                return true
            }
        }
        vto.addOnPreDrawListener(listener)
    }
}
