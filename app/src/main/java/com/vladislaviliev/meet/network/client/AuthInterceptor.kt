package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.repositories.LoginRepository
import com.vladislaviliev.meet.network.sign
import okhttp3.Interceptor
import okhttp3.Response

internal class AuthInterceptor(private val loginRepository: () -> LoginRepository?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val loginRepository = loginRepository()
        if (loginRepository == null) {
            return chain.proceed(chain.request())
        }
        val accessToken = loginRepository.tokens.value.access
        return chain.proceed(chain.request().sign(accessToken))
    }
}