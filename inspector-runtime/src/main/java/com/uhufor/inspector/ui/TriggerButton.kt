package com.uhufor.inspector.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

internal class TriggerButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val button: ImageView
    private val label: TextView

    init {
        orientation = VERTICAL

        button = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_search)
        }
        label = TextView(context).apply {
            textSize = 9F
            setTextColor(Color.RED)
        }
        addView(button, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        addView(label, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
    }

    fun setClickListener(listener: () -> Unit) {
        button.setOnClickListener { listener.invoke() }
    }

    fun setLongClickListener(listener: () -> Boolean) {
        button.setOnLongClickListener {
            listener.invoke()
        }
    }

    fun setLabelText(text: String) {
        label.text = text
    }
}

