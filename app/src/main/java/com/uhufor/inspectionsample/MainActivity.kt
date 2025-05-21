package com.uhufor.inspectionsample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
}
