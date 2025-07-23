package com.vladislaviliev.meet.ui.chips.overview

import androidx.annotation.DimenRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.vladislaviliev.meet.R

@Composable
internal fun OverviewChipType.icon(@DimenRes iconSize: Int): @Composable () -> Unit {

    val icon = when (this) {
        OverviewChipType.Payment -> Icons.Filled.Receipt
        OverviewChipType.Date -> Icons.Filled.DateRange
        OverviewChipType.Time -> Icons.Filled.Schedule
        OverviewChipType.Participants -> Icons.Filled.Group
        OverviewChipType.Location -> Icons.Filled.LocationOn
        OverviewChipType.Accessibility -> Icons.Filled.Public
        OverviewChipType.ConfirmLocation -> Icons.AutoMirrored.Filled.Send
    }

    return { Icon(icon, iconDescription(), Modifier.size(dimensionResource(iconSize))) }
}

@Composable
internal fun OverviewChipType.iconDescription() = when (this) {
    OverviewChipType.Payment -> stringResource(R.string.payment)
    OverviewChipType.Date -> stringResource(R.string.date)
    OverviewChipType.Time -> stringResource(R.string.time)
    OverviewChipType.Location -> stringResource(R.string.location)
    OverviewChipType.Participants -> stringResource(R.string.participants)
    OverviewChipType.Accessibility -> stringResource(R.string.accessibility)
    OverviewChipType.ConfirmLocation -> stringResource(R.string.confirm_location)
}