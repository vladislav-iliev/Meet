package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.repositories.LoginRepository
import com.vladislaviliev.meet.network.sign
import okhttp3.Interceptor

internal class AuthInterceptor(private val loginRepository: LoginRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain) =
        chain.proceed(chain.request().sign(loginRepository.tokens.value.access))
}