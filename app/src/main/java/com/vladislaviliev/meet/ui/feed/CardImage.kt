package com.vladislaviliev.meet.ui.feed

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.vladislaviliev.meet.R

@Composable
internal fun CardImage(uri: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier.clip(MaterialTheme.shapes.small),
        contentScale = ContentScale.FillWidth,
        placeholder = painterResource(R.drawable.ic_launcher_background)
    )
}