package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.Tokens
import com.vladislaviliev.meet.network.sign
import okhttp3.Interceptor


internal class AuthInterceptor(private val tokens: Tokens) : Interceptor {
    override fun intercept(chain: Interceptor.Chain) = chain.proceed(chain.request().sign(tokens.access))
}