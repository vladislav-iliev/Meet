package com.vladislaviliev.meet.session

import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryProvider
import okhttp3.OkHttpClient
import org.koin.core.Koin
import org.koin.core.scope.Scope

internal class SessionRepository(
    private val koin: Koin,
    private val client: OkHttpClient,
    private val loginRepositoryProvider: LoginRepositoryProvider
) {
    var currentScope: Scope? = null
        private set

    val isSessionActive get() = currentScope != null

    fun startSession() {
        endSession()
        currentScope = koin.createScope<Session>()
    }

    fun endSession() {
        client.dispatcher.cancelAll()
        loginRepositoryProvider.update(null)
        currentScope?.close()
        currentScope = null
    }
}