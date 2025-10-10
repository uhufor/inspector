package com.uhufor.inspector.ui

internal sealed class EditMode {
    object None : EditMode()
    object MarginPadding : EditMode()
    object Text : EditMode()
}
