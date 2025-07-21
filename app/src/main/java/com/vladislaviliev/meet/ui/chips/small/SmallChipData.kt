package com.vladislaviliev.meet.ui.chips.small

import androidx.compose.runtime.Composable
import com.vladislaviliev.meet.ui.chips.overview.OverviewChipType
import com.vladislaviliev.meet.ui.chips.overview.icon
import com.vladislaviliev.meet.ui.chips.overview.iconDescription
import com.vladislaviliev.meet.ui.chips.overview.overviewChipText
import org.openapitools.client.models.PostResponseDto

internal data class SmallChipData(val icon: @Composable () -> Unit, val contentDescription: String, val text: String)

@Composable
internal fun PostResponseDto.smallChipData(type: OverviewChipType) =
    SmallChipData(type.icon(), type.iconDescription(), overviewChipText(type))

@Composable
internal fun PostResponseDto.smallChipsData() = listOf(
    smallChipData(OverviewChipType.Payment),
    smallChipData(OverviewChipType.Date),
    smallChipData(OverviewChipType.Time),
    smallChipData(OverviewChipType.Participants),
    smallChipData(OverviewChipType.Location),
    smallChipData(OverviewChipType.Accessibility),
    smallChipData(OverviewChipType.ConfirmLocation)
)
