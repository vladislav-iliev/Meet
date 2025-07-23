package com.vladislaviliev.meet.ui.chips.small

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.vladislaviliev.meet.R
import org.openapitools.client.models.Interest
import org.openapitools.client.models.PostResponseDto

@Composable
internal fun PostResponseDto.interestChipsData() =
    interests.map { SmallChipData(it.chipIcon(), it.name, it.name) }.toList()

@Composable
private fun Interest.chipIcon(): @Composable () -> Unit = {
    AsyncImage(
        icon,
        name,
        placeholder = painterResource(R.drawable.ic_launcher_background),
        error = painterResource(R.drawable.ic_launcher_background),
        modifier = Modifier.size(dimensionResource(R.dimen.small_chip_icon))
    )
}