package com.uhufor.inspectionsample

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uhufor.inspectionsample.contact.ContactActivity
import com.uhufor.inspectionsample.contact.ContactComposeActivity

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
                Intent(this, ContactActivity::class.java)
            )
        }
        findViewById<View>(R.id.showComposeUi).setOnClickListener {
            startActivity(
                Intent(this, ContactComposeActivity::class.java)
            )
        }
    }
}
