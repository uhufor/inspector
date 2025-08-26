package com.uhufor.inspector.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

// Non scaled pixel
val TextUnit.nsp
    @Composable
    get() = (this.value / LocalDensity.current.fontScale).sp

val Int.nsp
    @Composable
    get() = this.sp.nsp

val Float.nsp
    @Composable
    get() = this.sp.nsp

val Double.nsp
    @Composable
    get() = this.sp.nsp
