package com.vladislaviliev.meet.ui.feed

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vladislaviliev.meet.R

@Composable
internal fun CardButtons(modifier: Modifier = Modifier) {
    Row(modifier) {
        IconButton({}) { Icon(Icons.Filled.People, stringResource(R.string.group)) }
        IconButton({}) { Icon(Icons.Filled.PinDrop, stringResource(R.string.pin)) }
        IconButton({}) { Icon(Icons.AutoMirrored.Filled.Send, stringResource(R.string.location)) }
        Spacer(Modifier.weight(1f))
        IconButton({}) { Icon(Icons.Filled.BookmarkBorder, stringResource(R.string.bookmark)) }
    }
}