package com.uhufor.inspector.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.uhufor.inspector.ui.compose.dvdp
import com.uhufor.inspector.ui.compose.dvsp

@Composable
internal fun Row1EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    labelWeight: Float = 0.6f,
    fieldWeight: Float = 1.0f,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 8.dvsp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(labelWeight)
        )
        SmallTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(fieldWeight, fill = true),
            keyboardOptions = keyboardOptions,
        )
    }
}

// TODO: make more common function as Row2EditField and remove margin/padding naming
@Composable
internal fun EditFieldRow(
    label: String,
    marginValue: String,
    onMarginChange: (String) -> Unit,
    paddingValue: String,
    onPaddingChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    labelWeight: Float = 0.6f,
    fieldWeight: Float = 1f,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dvdp)
    ) {
        Text(
            text = label,
            fontSize = 8.dvsp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(labelWeight)
        )
        SmallNumberField(
            marginValue,
            onMarginChange,
            keyboardOptions,
            modifier = Modifier.weight(fieldWeight)
        )
        SmallNumberField(
            paddingValue,
            onPaddingChange,
            keyboardOptions,
            modifier = Modifier.weight(fieldWeight)
        )
    }
}
