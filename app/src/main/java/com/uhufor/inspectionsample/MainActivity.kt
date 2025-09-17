package com.uhufor.inspectionsample

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uhufor.inspectionsample.bottomsheet.PersonListBottomSheetDialogFragment
import com.uhufor.inspectionsample.contact.ContactActivity
import com.uhufor.inspectionsample.dialog.PersonListDialogFragment
import com.uhufor.inspectionsample.mixed.MixedComposeActivity
import com.uhufor.inspectionsample.mixed.MixedViewActivity
import com.uhufor.inspector.Inspector
import com.uhufor.inspector.RelativeGuideStyle

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
        findViewById<View>(R.id.showMixedA).setOnClickListener {
            startActivity(
                ContactActivity.newIntent(
                    context = this,
                    showProfileCompose = false,
                    showHistoryCompose = true
                )
            )
        }
        findViewById<View>(R.id.showMixedB).setOnClickListener {
            startActivity(
                MixedViewActivity.newIntent(this)
            )
        }
        findViewById<View>(R.id.showMixedC).setOnClickListener {
            startActivity(
                MixedComposeActivity.newIntent(this)
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

        setupDetailsViewUiScale()
        setupGuideStyleSelector()
    }

    private fun setupDetailsViewUiScale() {
        val uiScaleTextView = findViewById<TextView>(R.id.uiScale)
        val uiScaleSeekBar = findViewById<SeekBar>(R.id.uiScaleSeekBar)

        val initialScale = Inspector.getDetailsViewUiScale().coerceIn(0.8f, 1.2f)
        uiScaleTextView.text = "UI Scale: %.2f".format(initialScale)
        uiScaleSeekBar.progress = (initialScale * 100f).toInt().coerceIn(80, 120)

        uiScaleSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    val currentValue = progress.coerceIn(80, 120)
                    val uiScale = currentValue.toFloat() / 100f
                    uiScaleTextView.text = "UI Scale: %.2f".format(uiScale)
                    Inspector.setDetailsViewUiScale(uiScale)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            }
        )
    }

    private fun setupGuideStyleSelector() {
        val group = findViewById<android.widget.RadioGroup>(R.id.guideStyleGroup)
        val none = findViewById<android.widget.RadioButton>(R.id.guideNone)
        val edge = findViewById<android.widget.RadioButton>(R.id.guideEdge)
        val full = findViewById<android.widget.RadioButton>(R.id.guideFull)

        when (Inspector.getRelativeGuideStyle()) {
            RelativeGuideStyle.NONE -> group.check(none.id)
            RelativeGuideStyle.EDGE -> group.check(edge.id)
            RelativeGuideStyle.FULL -> group.check(full.id)
        }

        group.setOnCheckedChangeListener { _, checkedId ->
            val newStyle = when (checkedId) {
                none.id -> RelativeGuideStyle.NONE
                edge.id -> RelativeGuideStyle.EDGE
                full.id -> RelativeGuideStyle.FULL
                else -> return@setOnCheckedChangeListener
            }
            Inspector.setRelativeGuideStyle(newStyle)
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
        if (Inspector.isInspectionEnabled) {
            Inspector.disableInspection()
        }
        Inspector.hideFloatingTrigger()
    }
}
