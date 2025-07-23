package com.vladislaviliev.meet.ui.chips.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vladislaviliev.meet.R
import org.openapitools.client.models.PostResponseDto

@Composable
internal fun PostResponseDto.overviewChipDescription(type: OverviewChipType) =
    when (type) {
        OverviewChipType.Payment -> payment(this)
        OverviewChipType.Date -> date(this)
        OverviewChipType.Time -> date(this)
        OverviewChipType.Location -> location(this)
        OverviewChipType.Participants -> ""
        OverviewChipType.Accessibility -> accessibility(this)
        OverviewChipType.ConfirmLocation -> stringResource(R.string.you_will_have_to_confirm_your_attendance)
    }

@Composable
private fun payment(post: PostResponseDto) =
    if (post.payment == 0.0) stringResource(R.string.no_payment_required) else stringResource(R.string.payment_required)

@Composable
private fun date(post: PostResponseDto) = post.overviewSmallChipText(OverviewChipType.Time)

private fun location(post: PostResponseDto) = "${post.location.name}, ${post.location.city}"

@Composable
private fun accessibility(post: PostResponseDto) = stringResource(
    if (post.accessibility == PostResponseDto.Accessibility.PRIVATE) R.string.only_invited_people_can_join
    else R.string.everyone_can_join_the_event
)
