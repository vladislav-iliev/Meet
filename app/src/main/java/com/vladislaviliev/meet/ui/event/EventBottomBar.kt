package com.vladislaviliev.meet.ui.event

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.theme.MeetTheme

@Composable
internal fun EventBottomBar(modifier: Modifier = Modifier) {
    BottomAppBar(modifier) {
        Button({}, Modifier.weight(1f)) { Text(stringResource(R.string.join)) }
        IconButton({}) { Icon(Icons.AutoMirrored.Filled.Send, stringResource(R.string.send)) }
        IconButton({}) { Icon(Icons.Filled.BookmarkBorder, stringResource(R.string.bookmark)) }
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL, showBackground = true)
@Composable
private fun Preview() {
    MeetTheme {
        EventBottomBar()
    }
}