package com.vladislaviliev.meet.ui.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.theme.MeetTheme

@Composable
internal fun LoadingScreen(modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.loading))
            LinearProgressIndicator()
        }
    }
}

@Preview
@Composable
private fun Preview() {
    MeetTheme {
        LoadingScreen()
    }
}