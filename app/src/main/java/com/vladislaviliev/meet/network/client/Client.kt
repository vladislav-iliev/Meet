package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.repositories.LoginRepository
import com.vladislaviliev.meet.network.repositories.LoginRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class Client(
    scope: CoroutineScope,
    loginRepositoryProvider: LoginRepositoryProvider,
    onDisconnect: () -> Unit
) {

    init {
        loginRepositoryProvider.current.onEach { repo = it }.launchIn(scope)
    }

    private var repo: LoginRepository? = null

    val instance: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .addInterceptor(AuthInterceptor({ repo }))
        .authenticator(Authenticator({ repo }, onDisconnect))
        .build()
}