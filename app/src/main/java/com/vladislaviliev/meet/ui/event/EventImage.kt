package com.vladislaviliev.meet.ui.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.vladislaviliev.meet.R

@Composable
internal fun EventImage(uri: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = uri,
        contentDescription = "Event Image",
        modifier,
        contentScale = ContentScale.FillWidth,
        placeholder = painterResource(R.drawable.image_downloading),
        error = painterResource(R.drawable.image_broken),
    )
}