package com.vladislaviliev.meet.ui.chips.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vladislaviliev.meet.R
import org.openapitools.client.models.PostResponseDto
import java.time.format.DateTimeFormatter

@Composable
internal fun PostResponseDto.overviewChipText(type: OverviewChipType) = when (type) {
    OverviewChipType.Payment -> payment(this)
    OverviewChipType.Date -> date(this)
    OverviewChipType.Time -> time(this)
    OverviewChipType.Location -> location(this)
    OverviewChipType.Participants -> participants(this)
    OverviewChipType.Accessibility -> accessibility(this)
    OverviewChipType.ConfirmLocation -> stringResource(R.string.confirm_location)
}

@Composable
private fun payment(post: PostResponseDto): String {
    val payment = post.payment
    return if (payment == 0.0) stringResource(R.string.free) else payment.toString()
}

private fun date(post: PostResponseDto): String {
    val from = post.fromDate!!
    val to = post.toDate!!
    val formatter = DateTimeFormatter.ofPattern("MM")
    return "${from.dayOfMonth}.${from.format(formatter)} - ${to.dayOfMonth}.${to.format(formatter)}"
}

private fun time(post: PostResponseDto): String {
    val fromTime = post.fromDate!!
    val toTime = post.toDate!!
    return "${fromTime.hour}:${fromTime.minute} - ${toTime.hour}:${toTime.minute}"
}

@Composable
private fun participants(post: PostResponseDto): String {
    val participants = post.participantsCount
    val max = post.maximumPeople
    return stringResource(R.string.participants) + " " + if (max == null) participants.toString() else "$participants/$max"
}

private fun location(post: PostResponseDto): String {
    val location = post.location
    val name = location.name
    val city = location.city
    val country = location.country

    val builder = StringBuilder()
    builder.append(name)
    if (city != null) builder.append(", $city")
    if (country != null) builder.append(", $country")
    return builder.toString()
}

@Composable
private fun accessibility(post: PostResponseDto) = stringResource(
    if (post.accessibility == PostResponseDto.Accessibility.PRIVATE) R.string._private
    else R.string._public
)
