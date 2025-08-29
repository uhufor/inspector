package com.uhufor.inspector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.uhufor.inspector.R
import com.uhufor.inspector.ui.compose.dvdp
import com.uhufor.inspector.ui.compose.dvsp

@Composable
internal fun SmallNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(36.dvdp)
            .height(20.dvdp)
            .padding(vertical = 0.5.dvdp, horizontal = 2.dvdp)
            .background(
                colorResource(R.color.inspector_number_field_bg),
                shape = RoundedCornerShape(2.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = colorResource(R.color.inspector_number_field_text),
                fontSize = 9.dvsp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dvdp)
        )
    }
}
