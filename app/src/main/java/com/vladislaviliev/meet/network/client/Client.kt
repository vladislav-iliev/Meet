package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class Client(loginRepositoryProvider: LoginRepositoryProvider, quit: () -> Unit) {

    val instance: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .addInterceptor(AuthInterceptor(loginRepositoryProvider.current::value))
        .authenticator(Authenticator(loginRepositoryProvider.current::value, quit))
        .build()
}