package com.uhufor.inspectionsample.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uhufor.inspectionsample.R
import com.uhufor.inspectionsample.adapter.PersonListAdapter
import com.uhufor.inspectionsample.model.DummyData

class PersonListBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_person_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PersonListAdapter(DummyData.getPersonList())
    }

    companion object {
        const val TAG = "PersonListBottomSheetDialogFragment"

        fun newInstance(): PersonListBottomSheetDialogFragment {
            return PersonListBottomSheetDialogFragment()
        }
    }
}
