package com.vladislaviliev.meet.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ChipColors
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.vladislaviliev.meet.R

data class ChipData(val type: ChipType, val text: String)

@Composable
internal fun Chips(data: List<ChipData>, modifier: Modifier = Modifier) {
    val colors = SuggestionChipDefaults.suggestionChipColors().copy(
        disabledContainerColor = SuggestionChipDefaults.suggestionChipColors().containerColor,
        disabledLabelColor = SuggestionChipDefaults.suggestionChipColors().labelColor,
        disabledLeadingIconContentColor = SuggestionChipDefaults.suggestionChipColors().leadingIconContentColor
    )
    FlowRow(modifier, Arrangement.spacedBy(6.dp), Arrangement.spacedBy(6.dp)) {
        data.forEach { Chip(it.type, it.text, colors) }
    }
}

@Composable
private fun Chip(type: ChipType, text: String, colors: ChipColors, modifier: Modifier = Modifier) {

    val icon = when (type) {
        ChipType.Payment -> Icons.Filled.Receipt
        ChipType.Date -> Icons.Filled.DateRange
        ChipType.Time -> Icons.Filled.Schedule
        ChipType.Participants -> Icons.Filled.Group
        ChipType.Location -> Icons.Filled.LocationOn
        ChipType.Accessibility -> Icons.Filled.Public
        ChipType.ConfirmLocation -> Icons.AutoMirrored.Filled.Send
    }

    val contentDescription = when (type) {
        ChipType.Payment -> stringResource(R.string.payment)
        ChipType.Date -> stringResource(R.string.date)
        ChipType.Time -> stringResource(R.string.time)
        ChipType.Participants -> stringResource(R.string.participants)
        ChipType.Location -> stringResource(R.string.location)
        ChipType.Accessibility -> stringResource(R.string.accessibility)
        ChipType.ConfirmLocation -> stringResource(R.string.confirm_location)
    }

    SuggestionChip(
        onClick = {},
        label = { Text(text) },
        modifier = modifier
            .requiredHeightIn(max = 32.dp)
            .clearAndSetSemantics { this.contentDescription = contentDescription },
        enabled = false,
        icon = { Icon(icon, null) },
        colors = colors,
    )
}
