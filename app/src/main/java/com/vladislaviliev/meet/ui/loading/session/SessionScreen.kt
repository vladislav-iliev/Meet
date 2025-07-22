package com.vladislaviliev.meet.ui.loading.session

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vladislaviliev.meet.ui.loading.LoadingScreen
import com.vladislaviliev.meet.ui.loading.LoadingState
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SessionScreen(onSessionRestarted: () -> Unit) {
    val viewModel = koinViewModel<SessionViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state is LoadingState.Success) {
        onSessionRestarted()
        return
    }
    LoadingScreen({}, LoadingState.Loading)
}