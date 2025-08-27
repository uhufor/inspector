package com.uhufor.inspector.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.uhufor.inspector.ui.compose.LocalDetailsViewUiScale

@Stable
inline val TextUnit.dvsp
    @Composable
    get() = (this.value / LocalDensity.current.fontScale * LocalDetailsViewUiScale.current).sp

@Stable
inline val Int.dvsp
    @Composable
    get() = this.sp.dvsp

@Stable
inline val Double.dvsp
    @Composable
    get() = this.sp.dvsp

@Stable
inline val Float.dvsp
    @Composable
    get() = this.sp.dvsp
