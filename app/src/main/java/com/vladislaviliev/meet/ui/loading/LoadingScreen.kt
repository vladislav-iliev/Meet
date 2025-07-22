package com.vladislaviliev.meet.ui.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.theme.MeetTheme

@Composable
internal fun LoadingScreen(onRetryClick: () -> Unit, state: LoadingState, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxSize()) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            StateIndicator(onRetryClick, state)
        }
    }
}

@Composable
private fun StateIndicator(onRetryClick: () -> Unit, state: LoadingState) {
    val spacer: @Composable () -> Unit = { Spacer(Modifier.height(16.dp)) }

    if (state is LoadingState.Loading) {
        Text(stringResource(R.string.loading))
        spacer()
        CircularProgressIndicator()
        return
    }
    require(state is LoadingState.Error)
    Text(state.message, color = MaterialTheme.colorScheme.error)
    spacer()
    Button(onRetryClick) { Text(stringResource(R.string.retry)) }
}

@Preview
@Composable
private fun Preview() {
    MeetTheme {
        LoadingScreen({}, LoadingState.Loading)
    }
}