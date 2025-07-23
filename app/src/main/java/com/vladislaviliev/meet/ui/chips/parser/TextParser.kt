package com.vladislaviliev.meet.ui.chips.parser

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.chips.OverviewChipType
import org.openapitools.client.models.PostResponseDto
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

internal class TextParser {

    @Composable
    fun smallChipText(type: OverviewChipType, post: PostResponseDto) = when (type) {
        OverviewChipType.Payment -> payment(post)
        OverviewChipType.Date -> date(post)
        OverviewChipType.Time -> time(post)
        OverviewChipType.Location -> location(post)
        OverviewChipType.Participants -> participants(post)
        OverviewChipType.Accessibility -> accessibility(post)
        OverviewChipType.ConfirmLocation -> stringResource(R.string.confirm_location)
    }

    @Composable
    fun bigChipText(type: OverviewChipType, post: PostResponseDto) = when (type) {
        OverviewChipType.Participants -> "Participants " + smallChipText(OverviewChipType.Participants, post)
        else -> smallChipText(type, post)
    }

    @Composable
    fun description(type: OverviewChipType, post: PostResponseDto) = DescriptionParser(this).parse(post, type)

    @Composable
    private fun payment(post: PostResponseDto): String {
        val payment = post.payment
        val currency = post.currency?.code ?: ""
        return if (payment == 0.0) stringResource(R.string.free) else "${payment.roundToInt()} $currency"
    }

    private fun date(post: PostResponseDto): String {
        val from = post.fromDate!!
        val to = post.toDate
        val formatter = DateTimeFormatter.ofPattern("dd.MM")
        if (null == to)
            return "${from.format(formatter)}"
        return "${from.format(formatter)} - ${to.format(formatter)}"
    }

    private fun time(post: PostResponseDto): String {
        val from = post.fromDate!!
        val to = post.toDate
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        if (null == to)
            return "${from.format(formatter)}"
        return "${from.format(formatter)} - ${to.format(formatter)}"
    }

    @Composable
    private fun participants(post: PostResponseDto): String {
        val participants = post.participantsCount
        val max = post.maximumPeople
        return if (max == null) participants.toString() else "$participants/$max"
    }

    private fun location(post: PostResponseDto): String {
        val location = post.location
        val name = location.name
        val city = location.city
        val country = location.country

        val builder = StringBuilder()
        if (name.isNotEmpty()) builder.append("$name, ")
        if (city != null && city.isNotEmpty()) builder.append("$city, ")
        if (country != null && country.isNotEmpty()) builder.append(country)
        return builder.toString()
    }

    @Composable
    private fun accessibility(post: PostResponseDto) = stringResource(
        if (post.accessibility == PostResponseDto.Accessibility.PRIVATE) R.string._private
        else R.string._public
    )
}