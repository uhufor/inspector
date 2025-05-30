package com.uhufor.inspectionsample.contact

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uhufor.inspectionsample.R

class ContactComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.contact_compose_activity)
        setupInsets()

        if (savedInstanceState == null) {
            val profileFragment = ProfileFragment.newInstance(showCompose = true)
            val historyFragment = HistoryFragment.newInstance(showCompose = true)

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
}
