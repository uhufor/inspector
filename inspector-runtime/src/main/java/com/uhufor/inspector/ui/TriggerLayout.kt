package com.uhufor.inspector.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import com.uhufor.inspector.databinding.LayoutTriggerButtonBinding
import com.uhufor.inspector.util.FloatingViewDragHelper
import com.uhufor.inspector.util.dp

internal class TriggerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val binding: LayoutTriggerButtonBinding =
        LayoutTriggerButtonBinding.inflate(LayoutInflater.from(context), this)

    private var dragHelper: FloatingViewDragHelper? = null

    private var clickAction: (() -> Unit)? = null
    private var longClickAction: (() -> Unit)? = null

    init {
        binding.root.setBackgroundColor(Color.WHITE)
        binding.root.elevation = LAYOUT_ELEVATION.dp()
        binding.label.textSize = LABEL_TEXT_SIZE.dp()
        binding.label.setTextColor(Color.RED)
        setPadding(LAYOUT_PADDING)
    }

    fun setFloatingViewDragHelper(helper: FloatingViewDragHelper) {
        dragHelper = helper
    }

    fun setOnClickAction(action: () -> Unit) {
        this.clickAction = action
        binding.root.setOnClickListener {
            if (dragHelper?.isDragging == false) {
                clickAction?.invoke()
            }
        }
    }

    fun setOnLongClickAction(action: () -> Unit) {
        this.longClickAction = action
        binding.root.setOnLongClickListener {
            if (dragHelper?.isDragging == false) {
                longClickAction?.invoke()
                true
            } else {
                false
            }
        }
    }

    fun setLabelText(text: String) {
        binding.label.text = text
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupTouchListener()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setOnTouchListener(null)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        this.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dragHelper?.onDown(event)
                }

                MotionEvent.ACTION_MOVE -> {
                    dragHelper?.onMove(event)
                }

                MotionEvent.ACTION_UP -> {
                    dragHelper?.onUp()
                }

                else -> Unit
            }
            false
        }
    }

    companion object {
        private const val LAYOUT_ELEVATION = 4
        private const val LABEL_TEXT_SIZE = 2
        private const val LAYOUT_PADDING = 12
    }
}

