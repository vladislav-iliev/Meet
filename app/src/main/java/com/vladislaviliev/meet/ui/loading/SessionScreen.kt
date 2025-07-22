package com.vladislaviliev.meet.ui.loading

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun SessionScreen(onSessionRestarted: () -> Unit) {
    val viewModel = koinViewModel<SessionViewModel>()
    val isSessionActive by viewModel.isSessionActive.collectAsStateWithLifecycle()
    if (isSessionActive) {
        onSessionRestarted()
        return
    }
    LoadingScreen()
}