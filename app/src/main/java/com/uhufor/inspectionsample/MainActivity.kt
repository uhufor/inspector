package com.uhufor.inspectionsample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uhufor.inspectionsample.bottomsheet.PersonListBottomSheetDialogFragment
import com.uhufor.inspectionsample.contact.ContactActivity
import com.uhufor.inspectionsample.dialog.PersonListDialogFragment
import com.uhufor.inspector.Inspector

class MainActivity : AppCompatActivity() {

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
        findViewById<View>(R.id.showBottomSheetUi).setOnClickListener {
            PersonListBottomSheetDialogFragment.newInstance()
                .show(supportFragmentManager, PersonListBottomSheetDialogFragment.TAG)
        }
        findViewById<View>(R.id.showDialogUi).setOnClickListener {
            PersonListDialogFragment.createDialog(context = this)
                .show()
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
