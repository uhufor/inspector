package com.uhufor.inspector.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uhufor.inspector.R
import com.uhufor.inspector.ui.compose.dvdp
import com.uhufor.inspector.ui.compose.dvsp

@Composable
internal fun SectionHeader(
    title: String,
    rightContent: (@Composable () -> Unit),
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        SectionTitle(title = title)
        Spacer(modifier = Modifier.weight(1f))
        rightContent()
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SectionHeaderPreview() {
    SectionHeader(
        title = "Hello",
        rightContent = {}
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SectionHeaderWithButtonPreview() {
    SectionHeader(
        title = "Hello",
        rightContent = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        color = colorResource(R.color.inspector_button_bg),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 10.dvdp, vertical = 2.dvdp)
            ) {
                Text(
                    text = stringResource(R.string.inspector_action_edit),
                    fontSize = 8.dvsp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SectionHeaderWithIconPreview() {
    SectionHeader(
        title = "Hello",
        rightContent = {
            Image(
                painterResource(R.drawable.ic_compose),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(10.dvdp),
            )
        }
    )
}
