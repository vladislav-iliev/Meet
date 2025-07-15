package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.Tokens
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class Client(tokens: Tokens, renewToken: () -> String, onDisconnect: () -> Unit) {

    val instance: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .addInterceptor(AuthInterceptor(tokens))
        .authenticator(Authenticator(renewToken, onDisconnect))
        .build()
}