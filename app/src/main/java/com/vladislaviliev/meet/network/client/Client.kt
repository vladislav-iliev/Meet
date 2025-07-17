package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.repositories.LoginRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class Client(loginRepository: LoginRepository, onDisconnect: () -> Unit) {
    val instance: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .addInterceptor(AuthInterceptor(loginRepository))
        .authenticator(Authenticator(loginRepository, onDisconnect))
        .build()
}