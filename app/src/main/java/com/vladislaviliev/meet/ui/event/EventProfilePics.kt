package com.vladislaviliev.meet.ui.event

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vladislaviliev.meet.R

@Composable
fun EventProfilePics(uris: Iterable<String>, modifier: Modifier = Modifier) {
    Box(modifier) {
        uris.forEachIndexed { index, uri ->

            val imageSize = 48.dp
            val overlapPercentage = 0.5f

            AsyncImage(
                model = uri,
                placeholder = painterResource(R.drawable.image_downloading),
                error = painterResource(R.drawable.image_broken),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .offset(x = if (index == 0) 0.dp else imageSize * (1 - overlapPercentage) * index)
                    .border(2.dp, CardDefaults.cardColors().containerColor, CircleShape)
                    .clip(CircleShape)
                    .size(imageSize)
                    .align(Alignment.CenterStart)
            )
        }
    }
}