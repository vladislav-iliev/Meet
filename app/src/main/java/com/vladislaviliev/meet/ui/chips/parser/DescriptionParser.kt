package com.vladislaviliev.meet.ui.chips.parser

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.chips.OverviewChipType
import org.openapitools.client.models.PostResponseDto

internal class DescriptionParser(private val textParser: TextParser) {

    @Composable
    fun parse(post: PostResponseDto, type: OverviewChipType) =
        when (type) {
            OverviewChipType.Payment -> payment(post)
            OverviewChipType.Date -> date(post)
            OverviewChipType.Time -> date(post)
            OverviewChipType.Location -> location(post)
            OverviewChipType.Participants -> ""
            OverviewChipType.Accessibility -> accessibility(post)
            OverviewChipType.ConfirmLocation -> stringResource(R.string.you_will_have_to_confirm_your_attendance)
        }

    @Composable
    private fun payment(post: PostResponseDto) =
        if (post.payment == 0.0) stringResource(R.string.no_payment_required) else stringResource(R.string.payment_required)

    @Composable
    private fun date(post: PostResponseDto) = textParser.smallChipText(OverviewChipType.Time, post)

    private fun location(post: PostResponseDto) = "${post.location.name}, ${post.location.city}"

    @Composable
    private fun accessibility(post: PostResponseDto) = stringResource(
        if (post.accessibility == PostResponseDto.Accessibility.PRIVATE) R.string.only_invited_people_can_join
        else R.string.everyone_can_join_the_event
    )
}