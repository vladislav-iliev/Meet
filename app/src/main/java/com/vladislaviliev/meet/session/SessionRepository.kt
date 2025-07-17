package com.vladislaviliev.meet.session

import android.util.Log
import com.vladislaviliev.meet.network.repositories.LoginRepositoryProvider
import org.koin.core.Koin
import org.koin.core.scope.Scope

internal class SessionRepository(
    private val koin: Koin,
    private val loginRepositoryProvider: LoginRepositoryProvider
) {
    private val logTag = this::class.simpleName
    var currentScope: Scope? = null
        private set

    val isSessionActive get() = currentScope != null

    fun startSession() {
        endSession()
        currentScope = koin.createScope<Session>()
        Log.d(logTag, "Session scope started")
    }

    fun endSession() {
        Log.d(logTag, "Stopping session scope")
        loginRepositoryProvider.update(null)
        currentScope?.close()
        currentScope = null
        Log.d(logTag, "Session scope stopped")
    }
}