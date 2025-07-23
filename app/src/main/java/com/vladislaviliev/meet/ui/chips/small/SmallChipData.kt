package com.vladislaviliev.meet.ui.chips.small

import androidx.compose.runtime.Composable
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.chips.overview.OverviewChipType
import com.vladislaviliev.meet.ui.chips.overview.icon
import com.vladislaviliev.meet.ui.chips.overview.iconDescription
import com.vladislaviliev.meet.ui.chips.overview.overviewSmallChipText
import org.openapitools.client.models.PostResponseDto

internal data class SmallChipData(val icon: @Composable () -> Unit, val contentDescription: String, val text: String)

@Composable
internal fun PostResponseDto.smallChipData(type: OverviewChipType) =
    SmallChipData(type.icon(R.dimen.small_chip_icon), type.iconDescription(), overviewSmallChipText(type))

@Composable
internal fun PostResponseDto.smallChipsData(): List<SmallChipData> {
    val list = mutableListOf(
        smallChipData(OverviewChipType.Payment),
        smallChipData(OverviewChipType.Participants),
        smallChipData(OverviewChipType.Location),
        smallChipData(OverviewChipType.Accessibility),
    )
    if (fromDate != null) {
        list.add(1, smallChipData(OverviewChipType.Date))
        list.add(2, smallChipData(OverviewChipType.Time))
    }
    if (this.needsLocationalConfirmation)
        list.add(smallChipData(OverviewChipType.ConfirmLocation))
    return list
}
