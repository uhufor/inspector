package com.uhufor.inspector.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import com.uhufor.inspector.databinding.LayoutTriggerBinding
import com.uhufor.inspector.util.FloatingViewDragHelper
import com.uhufor.inspector.util.dp

internal class TriggerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val binding: LayoutTriggerBinding =
        LayoutTriggerBinding.inflate(LayoutInflater.from(context), this)

    private var dragHelper: FloatingViewDragHelper? = null

    init {
        binding.root.setBackgroundColor(Color.WHITE)
        binding.root.elevation = LAYOUT_ELEVATION.dp()
        setPadding(LAYOUT_PADDING)
    }

    fun setFloatingViewDragHelper(helper: FloatingViewDragHelper) {
        dragHelper = helper
    }

    fun setButtonEnableState(buttonType: ButtonType, enable: Boolean) {
        when (buttonType) {
            ButtonType.INSPECTION -> {
                binding.toggleInspection.setBackgroundColor(
                    if (enable) INSPECTION_ENABLE_COLOR else Color.TRANSPARENT
                )
            }

            ButtonType.DP -> {
                binding.toggleDensity.text = if (enable) DENSITY_DP_TEXT else DENSITY_PX_TEXT
            }

            ButtonType.DFS -> {
                binding.toggleDfsTraverse.setTextColor(
                    if (enable) Color.RED else Color.BLACK
                )
            }

            ButtonType.SEE_PROPERTY_DETAILS -> {
                binding.toggleSeePropertyDetails.setTextColor(
                    if (enable) Color.RED else Color.BLACK
                )
            }
        }
    }

    fun setOnButtonClickListener(listener: (ButtonType) -> Unit) {
        binding.toggleInspection.setOnClickListener {
            if (dragHelper?.isDragging == false) {
                listener(ButtonType.INSPECTION)
            }
        }
        binding.toggleDensity.setOnClickListener {
            if (dragHelper?.isDragging == false) {
                listener(ButtonType.DP)
            }
        }
        binding.toggleDfsTraverse.setOnClickListener {
            if (dragHelper?.isDragging == false) {
                listener(ButtonType.DFS)
            }
        }
        binding.toggleSeePropertyDetails.setOnClickListener {
            if (dragHelper?.isDragging == false) {
                listener(ButtonType.SEE_PROPERTY_DETAILS)
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
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
        return false
    }

    internal enum class ButtonType {
        INSPECTION,
        DP,
        DFS,
        SEE_PROPERTY_DETAILS,
    }

    companion object {
        private const val LAYOUT_ELEVATION = 4
        private const val LAYOUT_PADDING = 12
        private const val INSPECTION_ENABLE_COLOR = 0x44FF0000
        private const val DENSITY_DP_TEXT = "DP"
        private const val DENSITY_PX_TEXT = "PX"
    }
}

