package com.uhufor.inspectionsample.mixed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uhufor.inspectionsample.R
import com.uhufor.inspectionsample.ui.theme.InspectionSampleTheme
import com.uhufor.inspector.Inspector
import kotlin.random.Random

class MixedComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        setContent {
            InspectionSampleTheme {
                MixedComposeScreen()
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
            return Intent(context, MixedComposeActivity::class.java)
        }
    }
}

private data class ComposeChatItem(
    val id: Int,
    val title: String,
    val icon: ImageVector,
    val leftAligned: Boolean,
)

@Composable
private fun MixedComposeScreen() {
    val inputBarHeight = 56.dp
    val items = remember {
        List(30) { index ->
            ComposeChatItem(
                id = index,
                title = "Item #${index + 1}",
                icon = Icons.Filled.Star,
                leftAligned = Random.nextBoolean()
            )
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(inputBarHeight),
                factory = { ctx ->
                    val root = LayoutInflater.from(ctx)
                        .inflate(R.layout.view_compose_mixed_input, null) as LinearLayout
                    val edit = root.findViewById<EditText>(R.id.et_input)
                    val btn = root.findViewById<Button>(R.id.btn_send)
                    btn.setOnClickListener {
                        val text = edit.text?.toString().orEmpty()
                        Toast.makeText(
                            ctx,
                            text.ifEmpty { ctx.getString(R.string.app_name) },
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    root
                },
                update = { }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            ChatList(
                items = items,
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    start = 12.dp,
                    end = 12.dp,
                    bottom = padding.calculateBottomPadding() + 72.dp
                )
            )
        }
    }
}

@Composable
private fun ChatList(items: List<ComposeChatItem>, contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        items(items, key = { it.id }) { item ->
            ChatRow(item)
        }
    }
}

@Composable
private fun ChatRow(item: ComposeChatItem) {
    val textColor = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (item.leftAligned) Arrangement.Start else Arrangement.End
    ) {
        if (item.leftAligned) {
            Image(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = item.title, color = textColor)
        } else {
            Text(text = item.title, color = textColor)
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
