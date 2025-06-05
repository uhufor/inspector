package com.uhufor.inspectionsample.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.uhufor.inspectionsample.R
import com.uhufor.inspectionsample.contact.compose.ProfileScreen
import com.uhufor.inspectionsample.ui.theme.InspectionSampleTheme

class ProfileFragment : Fragment() {

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
                    InspectionSampleTheme {
                        ProfileScreen()
                    }
                }
            }
        } else {
            inflater.inflate(R.layout.fragment_profile, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!showCompose) {
            val heartIcon = view.findViewById<ImageView>(R.id.heart_icon)
            heartIcon?.setOnClickListener {
                Toast.makeText(requireContext(), "Heart Clicked (XML)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val ARG_SHOW_COMPOSE = "show_compose"

        @JvmStatic
        fun newInstance(showCompose: Boolean) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_COMPOSE, showCompose)
                }
            }
    }
}
