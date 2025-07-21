package com.vladislaviliev.meet.ui.chips.small

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material3.ChipColors
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp

@Composable
internal fun chipColors() = SuggestionChipDefaults.suggestionChipColors().copy(
    disabledContainerColor = SuggestionChipDefaults.suggestionChipColors().containerColor,
    disabledLabelColor = SuggestionChipDefaults.suggestionChipColors().labelColor,
    disabledLeadingIconContentColor = SuggestionChipDefaults.suggestionChipColors().leadingIconContentColor,
    disabledTrailingIconContentColor = SuggestionChipDefaults.suggestionChipColors().trailingIconContentColor
)

@Composable
internal fun FlowRowSmallChips(data: List<SmallChipData>, modifier: Modifier = Modifier) {
    FlowRow(modifier, Arrangement.spacedBy(6.dp), Arrangement.spacedBy(6.dp)) {
        val colors = chipColors()
        data.forEach { SmallChip(it, colors = colors) }
    }
}

@Composable
internal fun SmallChip(data: SmallChipData, modifier: Modifier = Modifier, colors: ChipColors = chipColors()) {
    SuggestionChip(
        onClick = {},
        label = { Text(data.text) },
        modifier = modifier
            .requiredHeightIn(max = 32.dp)
            .clearAndSetSemantics { this.contentDescription = data.contentDescription },
        enabled = false,
        icon = data.icon,
        colors = colors,
    )
}
