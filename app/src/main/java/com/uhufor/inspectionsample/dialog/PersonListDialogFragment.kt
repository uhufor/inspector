package com.uhufor.inspectionsample.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uhufor.inspectionsample.R
import com.uhufor.inspectionsample.adapter.PersonListAdapter
import com.uhufor.inspectionsample.model.DummyData

class PersonListDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return createDialog(requireActivity())
    }

    companion object {
        const val TAG = "PersonListDialogFragment"

        fun newInstance(): PersonListDialogFragment {
            return PersonListDialogFragment()
        }

        fun createDialog(context: Context): Dialog {
            val builder = AlertDialog.Builder(context)
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.dialog_person_list, null)

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = PersonListAdapter(DummyData.getPersonList())

            builder.setView(view)
                .setTitle("Person List")
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
            return builder.create()
        }
    }
}
