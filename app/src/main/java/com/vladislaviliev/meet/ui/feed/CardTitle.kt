package com.vladislaviliev.meet.ui.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.vladislaviliev.meet.R

@Composable
internal fun CardTitle(avatarUri: String, title: String, userName: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Avatar(avatarUri)
        Spacer(Modifier.width(12.dp))
        Names(title, userName)
        OverflowBtn()
    }
}

@Composable
private fun Avatar(uri: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.FillWidth,
        placeholder = painterResource(R.drawable.ic_launcher_background),
        error = painterResource(R.drawable.ic_launcher_background)
    )
}

@Composable
fun RowScope.Names(title: String, userName: String, modifier: Modifier = Modifier) {
    Column(modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(userName, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun OverflowBtn(modifier: Modifier = Modifier) {
    IconButton({}, modifier) { Icon(Icons.Filled.MoreVert, stringResource(R.string.more)) }
}