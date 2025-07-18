package com.vladislaviliev.meet.network.repositories.login

import com.vladislaviliev.meet.network.Tokens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class LoginRepositoryTimer(
    private val scope: CoroutineScope,
    private val repository: LoginRepository,
    private val currentTime: () -> Long,
    private val eagerness: Long,
) {
    private var refreshJob: Job? = null

    init {
        repository.tokens.onEach { scheduleRefresh(it) }.launchIn(scope)
    }

    private fun scheduleRefresh(tokens: Tokens) {
        refreshJob?.cancel()
        refreshJob = null

        if (tokens == Tokens.BLANK) {
            return
        }

        val delay = tokens.expiry - eagerness - currentTime()
        if (delay < 0) {
            return
        }

        refreshJob = scope.launch {
            delay(delay)
            repository.refreshSync()
        }
    }
}