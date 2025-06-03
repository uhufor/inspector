package com.uhufor.inspectionsample

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uhufor.inspectionsample.contact.ContactActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main_activity)
        setupInsets()
        setupViews()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft + systemBars.left,
                v.paddingTop + systemBars.top,
                v.paddingRight + systemBars.right,
                v.paddingBottom + systemBars.bottom
            )
            insets
        }
    }

    private fun setupViews() {
        findViewById<View>(R.id.showXmlUi).setOnClickListener {
            startActivity(
                ContactActivity.newIntent(
                    context = this,
                    showProfileCompose = false,
                    showHistoryCompose = false
                )
            )
        }
        findViewById<View>(R.id.showComposeUi).setOnClickListener {
            startActivity(
                ContactActivity.newIntent(
                    context = this,
                    showProfileCompose = true,
                    showHistoryCompose = true
                )
            )
        }
    }
}
