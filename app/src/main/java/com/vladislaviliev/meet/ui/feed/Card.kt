package com.vladislaviliev.meet.ui.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vladislaviliev.meet.R
import org.openapitools.client.models.PostResponseDto

@Composable
internal fun CardItem(item: PostResponseDto, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column {

            val cardPadding = dimensionResource(R.dimen.feed_card_padding)

            CardTitle(
                item.owner.profilePhotos.first(),
                item.title,
                "${item.owner.firstName} ${item.owner.lastName}",
                Modifier
                    .fillMaxWidth()
                    .padding(cardPadding)
            )
            CardImage(
                item.images.first(),
                Modifier
                    .fillMaxWidth()
                    .padding(cardPadding, 0.dp)
            )
            CardButtons(
                Modifier
                    .fillMaxWidth()
                    .padding(cardPadding - 10.dp, 0.dp)
            )
            Chips(item, Modifier.padding(cardPadding, 0.dp, cardPadding, cardPadding))
        }
    }
}

@Composable
private fun Chips(item: PostResponseDto, modifier: Modifier = Modifier) {
    val chips = mutableListOf(
        ChipData(ChipType.Payment, payment(item)),
        ChipData(ChipType.Participants, participants(item)),
        ChipData(ChipType.Location, location(item)),
        ChipData(ChipType.Accessibility, accessibility(item)),
        ChipData(ChipType.ConfirmLocation, stringResource(R.string.confirm_location))
    )

    val dates = dates(item)
    val times = times(item)
    if (dates != null && times != null) {
        chips.add(1, ChipData(ChipType.Date, dates))
        chips.add(2, ChipData(ChipType.Time, times))
    }

    Chips(chips, modifier)
}

@Composable
private fun payment(item: PostResponseDto): String {
    val payment = item.payment
    return if (payment == 0.0) stringResource(R.string.free) else payment.toString()
}

private fun dates(item: PostResponseDto): String? {
    val fromDate = item.fromDate
    val toDate = item.toDate
    return if (fromDate == null) null
    else if (toDate == null) "${fromDate.dayOfMonth}.${fromDate.monthValue}"
    else "${fromDate.dayOfMonth}.${fromDate.monthValue} - ${toDate.dayOfMonth}.${toDate.monthValue}"
}

private fun times(item: PostResponseDto): String? {
    val fromTime = item.fromDate
    val toTime = item.toDate
    return if (fromTime == null) null
    else if (toTime == null) "${fromTime.hour}:${fromTime.minute}"
    else "${fromTime.hour}:${fromTime.minute} - ${toTime.hour}:${toTime.minute}"
}

private fun participants(item: PostResponseDto): String {
    val participants = item.participantsCount
    val max = item.maximumPeople
    return if (max == null) participants.toString() else "$participants/$max"
}

private fun location(item: PostResponseDto): String {
    val location = item.location
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
private fun accessibility(item: PostResponseDto) = stringResource(
    if (item.accessibility == PostResponseDto.Accessibility.PRIVATE) R.string._private
    else R.string._public
)
