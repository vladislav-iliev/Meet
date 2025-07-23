package com.vladislaviliev.meet.ui.chips.small

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun FlowRowSmallChips(data: List<SmallChipData>, modifier: Modifier = Modifier) {
    FlowRow(modifier, Arrangement.spacedBy(6.dp), Arrangement.spacedBy(6.dp)) {
        data.forEach { SmallChip(it) }
    }
}

@Composable
internal fun SmallChip(data: SmallChipData, modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(SuggestionChipDefaults.suggestionChipColors().containerColor, SuggestionChipDefaults.shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, SuggestionChipDefaults.shape)
            .padding(8.dp),
        Arrangement.spacedBy(6.dp),
        Alignment.CenterVertically,
    ) {
        data.icon()
        Text(data.text, style = MaterialTheme.typography.labelLarge)
    }
}
