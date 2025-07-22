package com.vladislaviliev.meet.ui.loading.user

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vladislaviliev.meet.ui.loading.LoadingScreen
import com.vladislaviliev.meet.ui.loading.LoadingState
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LoadingUserScreen(onLoaded: () -> Unit) {
    val vm = koinViewModel<LoadingUserViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    if (state is LoadingState.Success) {
        onLoaded()
        return
    }
    LoadingScreen(vm::download, state)
}