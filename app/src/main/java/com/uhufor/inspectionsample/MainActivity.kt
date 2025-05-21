package com.uhufor.inspectionsample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupInsets()
        setContentView(R.layout.main_activity)
        setupViews()
    }

    private fun setupViews() {
        findViewById<View>(R.id.click).setOnClickListener {
            Log.i("MainActivity", "Clicked Button")
        }
        findViewById<View>(R.id.clickCenter).setOnClickListener {
            Log.i("MainActivity", "Clicked Long Button")
        }
    }

    private fun setupInsets() {
        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }
}
