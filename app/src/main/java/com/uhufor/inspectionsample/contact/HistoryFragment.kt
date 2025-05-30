package com.uhufor.inspectionsample.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.uhufor.inspectionsample.R
import com.uhufor.inspectionsample.contact.compose.HistoryScreen
import com.uhufor.inspectionsample.contact.compose.sampleExperiences

class HistoryFragment : Fragment() {

    private var showCompose: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            showCompose = it.getBoolean(ARG_SHOW_COMPOSE, false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return if (showCompose) {
            ComposeView(requireContext()).apply {
                setContent {
                    HistoryScreen(experiences = sampleExperiences)
                }
            }
        } else {
            inflater.inflate(R.layout.fragment_history, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        private const val ARG_SHOW_COMPOSE = "show_compose"

        @JvmStatic
        fun newInstance(showCompose: Boolean) =
            HistoryFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_COMPOSE, showCompose)
                }
            }
    }
}
