package com.vladislaviliev.meet.ui.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.chips.parser.ChipParser
import com.vladislaviliev.meet.ui.chips.small.FlowRowSmallChips
import org.openapitools.client.models.PostResponseDto

@Composable
internal fun CardItem(onClick: (String) -> Unit, item: PostResponseDto, modifier: Modifier = Modifier) {
    Card({ onClick(item.id) }, modifier) {
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

            FlowRowSmallChips(
                ChipParser().smallChipsData(item),
                Modifier.padding(cardPadding, 0.dp, cardPadding, cardPadding)
            )
        }
    }
}