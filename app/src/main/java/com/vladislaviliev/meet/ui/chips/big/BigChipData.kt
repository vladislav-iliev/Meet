package com.vladislaviliev.meet.ui.chips.big

import androidx.compose.runtime.Composable
import com.vladislaviliev.meet.ui.chips.overview.OverviewChipType
import com.vladislaviliev.meet.ui.chips.overview.icon
import com.vladislaviliev.meet.ui.chips.overview.overviewChipDescription
import com.vladislaviliev.meet.ui.chips.overview.overviewChipText
import org.openapitools.client.models.PostResponseDto

internal data class BigChipData(val icon: @Composable () -> Unit, val text: String, val description: String)

@Composable
internal fun PostResponseDto.bigChipData(type: OverviewChipType) =
    BigChipData(type.icon(), overviewChipText(type), overviewChipDescription(type))
