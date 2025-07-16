package com.uhufor.inspectionsample.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uhufor.inspectionsample.R
import com.uhufor.inspectionsample.model.Person

class PersonListAdapter(private val items: List<Person>) :
    RecyclerView.Adapter<PersonListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_person, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.description.text = item.description
        holder.age.text = "Age: ${item.age}"
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.nameTextView)
        val description: TextView = itemView.findViewById(R.id.descriptionTextView)
        val age: TextView = itemView.findViewById(R.id.ageTextView)
    }
}
