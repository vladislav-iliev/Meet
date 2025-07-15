package com.vladislaviliev.meet.network

import okhttp3.OkHttpClient
import org.openapitools.client.apis.CognitoControllerApi

internal class TokenRefresh {
    private fun refreshAccess(client: OkHttpClient, refreshToken: String, userId: String) =
        CognitoControllerApi(client = client).refreshToken(refreshToken, userId).accessToken

    fun refreshTokens(client: OkHttpClient, refreshToken: String, userId: String): Tokens {
        val newAccess = refreshAccess(client, refreshToken, userId)
        val newExpiration = runCatching { TokenParser().parse(newAccess)["exp"]?.toLong() }.getOrNull() ?: -1L
        return Tokens(newAccess, refreshToken, newExpiration)
    }
}