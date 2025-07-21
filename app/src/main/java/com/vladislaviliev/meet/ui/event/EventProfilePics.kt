package com.vladislaviliev.meet.ui.event

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vladislaviliev.meet.R

@Composable
fun EventProfilePics(
    uris: Iterable<String>,
    modifier: Modifier = Modifier,
    imageSize: Dp = 48.dp,
    overlapPercentage: Float = 0.5f,
    outlineWidth: Dp = 2.dp,
    outlineColor: Color = MaterialTheme.colorScheme.surface
) {
    Box(modifier) {
        uris.forEachIndexed { index, uri ->
            AsyncImage(
                model = uri,
                placeholder = painterResource(R.drawable.ic_launcher_background),
                error = painterResource(R.drawable.ic_launcher_background),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .offset(x = if (index == 0) 0.dp else imageSize * (1 - overlapPercentage) * index)
                    .border(outlineWidth, outlineColor, CircleShape)
                    .clip(CircleShape)
                    .size(imageSize)
                    .align(Alignment.CenterStart)
            )
        }
    }
}