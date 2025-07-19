package com.vladislaviliev.meet.ui.feed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vladislaviliev.meet.R

@Composable
internal fun FeedBottomBar(modifier: Modifier = Modifier) {
    NavigationBar(modifier) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Filled.Home, stringResource(R.string.home)) })
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Filled.Map, stringResource(R.string.map)) })
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Filled.AddCircleOutline, stringResource(R.string.add)) })
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Filled.ChatBubbleOutline, stringResource(R.string.chat)) })
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Filled.AccountCircle, stringResource(R.string.profile)) })
    }
}