package com.vladislaviliev.meet.session

import org.koin.core.Koin
import org.koin.core.scope.Scope

internal class SessionRepository(private val koin: Koin) {
    var currentScope: Scope? = null
        private set

    val isSessionActive get() = currentScope != null

    fun restartSession() {
        endSession()
        currentScope = koin.createScope<Session>()
    }

    fun endSession() {
        currentScope?.close()
        currentScope = null
    }
}