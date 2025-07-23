package com.vladislaviliev.meet.ui.chips.big

import androidx.compose.runtime.Composable
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.chips.overview.OverviewChipType
import com.vladislaviliev.meet.ui.chips.overview.icon
import com.vladislaviliev.meet.ui.chips.overview.overviewBigChipText
import com.vladislaviliev.meet.ui.chips.overview.overviewChipDescription
import org.openapitools.client.models.PostResponseDto

internal data class BigChipData(val icon: @Composable () -> Unit, val text: String, val description: String)

@Composable
internal fun PostResponseDto.bigChipData(type: OverviewChipType) =
    BigChipData(type.icon(R.dimen.big_chip_icon), overviewBigChipText(type), overviewChipDescription(type))
