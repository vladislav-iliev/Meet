package com.vladislaviliev.meet.ui.chips.parser

import androidx.annotation.DimenRes
import androidx.compose.runtime.Composable
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.chips.OverviewChipType
import com.vladislaviliev.meet.ui.chips.big.BigChipData
import com.vladislaviliev.meet.ui.chips.small.SmallChipData
import org.openapitools.client.models.PostResponseDto

internal class ChipParser {

    private val iconParser = IconParser()
    private val textParser = TextParser()

    @Composable
    internal fun smallChipData(type: OverviewChipType, post: PostResponseDto) =
        SmallChipData(
            icon(type, R.dimen.small_chip_icon),
            description(type, post),
            textParser.smallChipText(type, post)
        )

    @Composable
    internal fun smallChipsData(post: PostResponseDto): List<SmallChipData> {
        val list = mutableListOf(
            smallChipData(OverviewChipType.Payment, post),
            smallChipData(OverviewChipType.Participants, post),
            smallChipData(OverviewChipType.Location, post),
            smallChipData(OverviewChipType.Accessibility, post),
        )
        if (post.fromDate != null) {
            list.add(1, smallChipData(OverviewChipType.Date, post))
            list.add(2, smallChipData(OverviewChipType.Time, post))
        }
        if (post.needsLocationalConfirmation)
            list.add(smallChipData(OverviewChipType.ConfirmLocation, post))
        return list
    }

    @Composable
    fun interestChipsData(post: PostResponseDto) =
        post.interests.map { SmallChipData(iconParser.icon(it), it.name, it.name) }.toList()

    @Composable
    fun bigChipData(type: OverviewChipType, post: PostResponseDto) =
        BigChipData(icon(type, R.dimen.big_chip_icon), textParser.bigChipText(type, post), description(type, post))

    @Composable
    fun icon(type: OverviewChipType, @DimenRes size: Int) = iconParser.icon(type, size)

    @Composable
    fun description(type: OverviewChipType, post: PostResponseDto) = textParser.description(type, post)
}