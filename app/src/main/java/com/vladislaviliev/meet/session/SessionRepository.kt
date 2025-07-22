package com.vladislaviliev.meet.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.Koin
import org.koin.core.scope.Scope

internal class SessionRepository(private val koin: Koin) {
    var currentScope: Scope? = null
        private set

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive = _isSessionActive.asStateFlow()

    fun restartSession() {
        endSession()
        currentScope = koin.createScope<Session>()
        _isSessionActive.value = true
    }

    fun endSession() {
        currentScope?.close()
        currentScope = null
        _isSessionActive.value = false
    }
}