package com.vladislaviliev.meet.ui.chips.small

import androidx.compose.runtime.Composable

internal data class SmallChipData(val icon: @Composable () -> Unit, val contentDescription: String, val text: String)
