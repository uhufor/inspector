package com.uhufor.inspector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import com.uhufor.inspector.R
import com.uhufor.inspector.ui.compose.dvdp
import com.uhufor.inspector.ui.compose.dvsp

@Composable
internal fun MarginPaddingBox(
    margin: List<String>,
    padding: List<String>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(colorResource(R.color.inspector_distance_margin_bg))
            .fillMaxWidth()
            .border(1.dvdp, colorResource(R.color.inspector_distance_line))
            .padding(4.dvdp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(margin[1], fontSize = 8.dvsp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    margin[0],
                    fontSize = 8.dvsp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(24.dvdp)
                )
                Spacer(modifier = Modifier.width(2.dvdp))
                Box(
                    modifier = Modifier
                        .weight(weight = 1.0f, fill = true)
                        .height(48.dvdp)
                        .padding(horizontal = 2.dvdp, vertical = 4.dvdp)
                        .border(1.dvdp, colorResource(R.color.inspector_distance_line))
                        .background(colorResource(R.color.inspector_distance_size_bg))
                        .padding(horizontal = 4.dvdp, vertical = 4.dvdp),
                ) {
                    Text(
                        padding[1],
                        fontSize = 8.dvsp,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                    Text(
                        padding[0],
                        fontSize = 8.dvsp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    Text(
                        padding[2],
                        fontSize = 8.dvsp,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                    Text(
                        padding[3],
                        fontSize = 8.dvsp,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }

                Spacer(modifier = Modifier.width(2.dvdp))
                Text(
                    margin[2],
                    fontSize = 8.dvsp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(24.dvdp)
                )
            }
            Text(margin[3], fontSize = 8.dvsp)
        }
    }
}
