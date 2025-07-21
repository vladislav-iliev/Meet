package com.vladislaviliev.meet.ui.event

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.theme.MeetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventTopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        {},
        modifier,
        { FilledIconButton({}) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
        { FilledIconButton({}) { Icon(Icons.Default.MoreVert, stringResource(R.string.more)) } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTopBar() {
    MeetTheme {
        EventTopBar()
    }
}
