package com.uhufor.inspectionsample.mixed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uhufor.inspectionsample.R
import com.uhufor.inspector.Inspector
import kotlin.random.Random

class MixedViewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mixed_view)
        setupInsets()
        disableInspectionIfNeeded(savedInstanceState)

        setupMainScreen()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun disableInspectionIfNeeded(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            if (Inspector.isInspectionEnabled) {
                Inspector.disableInspection()
            }
        }
    }

    private fun setupMainScreen() {
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        val adapter = ChatAdapter()
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
        val items = List(30) { index ->
            ViewChatItem(
                id = index,
                title = "Item #${index + 1}",
                leftAligned = Random.nextBoolean()
            )
        }
        adapter.submitList(items)

        val composeView = findViewById<ComposeView>(R.id.bottom_input_compose)
        composeView.setContent {
            MaterialTheme {
                Surface(shadowElevation = 0.dp) {
                    var text by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextField(
                            value = text,
                            onValueChange = { text = it },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            val v = text.ifEmpty { getString(R.string.app_name) }
                            Toast.makeText(this@MixedViewActivity, v, Toast.LENGTH_SHORT).show()
                        }) {
                            Text(text = "Send")
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Inspector.showFloatingTrigger()
    }

    override fun onPause() {
        super.onPause()
        Inspector.hideFloatingTrigger()
    }

    companion object {
        @JvmStatic
        fun newIntent(
            context: Context,
        ): Intent {
            return Intent(context, MixedViewActivity::class.java)
        }
    }
}

private data class ViewChatItem(val id: Int, val title: String, val leftAligned: Boolean)

private class ChatAdapter : ListAdapter<ViewChatItem, RecyclerView.ViewHolder>(Diff) {
    override fun getItemViewType(position: Int): Int = if (getItem(position).leftAligned) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            val v = inflater.inflate(R.layout.item_mixed_chat_left, parent, false)
            LeftVH(v)
        } else {
            val v = inflater.inflate(R.layout.item_mixed_chat_right, parent, false)
            RightVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is LeftVH -> holder.bind(item)
            is RightVH -> holder.bind(item)
        }
    }

    object Diff : DiffUtil.ItemCallback<ViewChatItem>() {
        override fun areItemsTheSame(oldItem: ViewChatItem, newItem: ViewChatItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ViewChatItem, newItem: ViewChatItem): Boolean =
            oldItem == newItem
    }

    private class LeftVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val text: TextView = itemView.findViewById(R.id.text)
        fun bind(item: ViewChatItem) {
            icon.setImageResource(R.drawable.ic_heart)
            text.text = item.title
        }
    }

    private class RightVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val text: TextView = itemView.findViewById(R.id.text)
        fun bind(item: ViewChatItem) {
            icon.setImageResource(com.uhufor.inspector.R.drawable.ic_compose)
            text.text = item.title
        }
    }
}
