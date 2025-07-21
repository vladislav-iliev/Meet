package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

internal class Client(loginRepositoryProvider: LoginRepositoryProvider) {

    val instance = run {
        val dispatcher = OkHttpClient.Builder()
            .build().dispatcher // obtain dispatcher like this in case the builders sprinkle some default magic on top
        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            .addInterceptor(AuthInterceptor(loginRepositoryProvider.current::value))
            .authenticator(Authenticator(loginRepositoryProvider.current::value))
            .build()
    }
}