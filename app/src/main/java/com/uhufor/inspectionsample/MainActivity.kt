package com.uhufor.inspectionsample

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uhufor.inspectionsample.contact.ContactActivity
import com.uhufor.inspector.Inspector

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        setupInsets()
        setupViews()
        disableInspectionIfNeeded(savedInstanceState)
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
        findViewById<View>(R.id.showCombinedUi).setOnClickListener {
            startActivity(
                ContactActivity.newIntent(
                    context = this,
                    showProfileCompose = false,
                    showHistoryCompose = true
                )
            )
        }
    }

    private fun disableInspectionIfNeeded(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (Inspector.isInspectionEnabled) {
                Inspector.disableInspection()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Inspector.install(this)
    }

    override fun onResume() {
        super.onResume()
        Inspector.showFloatingTrigger()
    }

    override fun onPause() {
        super.onPause()
        Inspector.hideFloatingTrigger()
    }
}
