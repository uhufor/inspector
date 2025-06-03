package com.uhufor.inspectionsample.contact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uhufor.inspectionsample.R

class ContactComposeActivity : AppCompatActivity() {

    private val showProfileCompose: Boolean
        get() = intent.getBooleanExtra(ARG_SHOW_PROFILE_COMPOSE, false)

    private val showHistoryCompose: Boolean
        get() = intent.getBooleanExtra(ARG_SHOW_HISTORY_COMPOSE, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contact_compose_activity)
        setupInsets()

        if (savedInstanceState == null) {
            val profileFragment = ProfileFragment.newInstance(showCompose = showProfileCompose)
            val historyFragment = HistoryFragment.newInstance(showCompose = showHistoryCompose)

            supportFragmentManager.beginTransaction()
                .replace(R.id.profile_fragment_container_compose, profileFragment)
                .replace(R.id.history_fragment_container_compose, historyFragment)
                .commit()
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_compose)) { v, insets ->
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

    companion object {
        private const val ARG_SHOW_PROFILE_COMPOSE = "show_profile_compose"
        private const val ARG_SHOW_HISTORY_COMPOSE = "show_profile_compose"

        @JvmStatic
        fun newIntent(
            context: Context,
            showProfileCompose: Boolean,
            showHistoryCompose: Boolean,
        ): Intent {
            return Intent(context, ContactComposeActivity::class.java).apply {
                putExtra(ARG_SHOW_PROFILE_COMPOSE, showProfileCompose)
                putExtra(ARG_SHOW_HISTORY_COMPOSE, showHistoryCompose)
            }
        }
    }
}
