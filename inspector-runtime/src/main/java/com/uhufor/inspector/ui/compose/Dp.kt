package com.uhufor.inspector.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp

@Stable
inline val Int.dvdp: Dp
    @Composable
    get() = Dp(this.toFloat() * LocalDetailsViewUiScale.current)

@Stable
inline val Double.dvdp: Dp
    @Composable
    get() = Dp(this.toFloat() * LocalDetailsViewUiScale.current)

@Stable
inline val Float.dvdp: Dp
    @Composable
    get() = Dp(this * LocalDetailsViewUiScale.current)
