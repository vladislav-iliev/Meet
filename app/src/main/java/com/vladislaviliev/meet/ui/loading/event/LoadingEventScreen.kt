package com.vladislaviliev.meet.ui.loading.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vladislaviliev.meet.event.EventScopeRepository
import com.vladislaviliev.meet.ui.loading.LoadingScreen
import com.vladislaviliev.meet.ui.loading.LoadingState
import org.koin.compose.getKoin

@Composable
internal fun LoadingEventScreen(onLoaded: () -> Unit, eventId: String) {
    val vm = getKoin()
        .get<EventScopeRepository>()
        .apply { restartIfNeeded(eventId) }
        .currentScope!!
        .get<LoadingEventViewModel>()

    val state by vm.state.collectAsStateWithLifecycle()
    if (state is LoadingState.Success) {
        onLoaded()
        return
    }
    LoadingScreen(vm::download, state)
}