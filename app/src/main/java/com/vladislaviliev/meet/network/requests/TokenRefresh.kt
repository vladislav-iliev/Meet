package com.vladislaviliev.meet.network.requests

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.Tokens
import okhttp3.OkHttpClient
import org.openapitools.client.apis.CognitoControllerApi

internal class TokenRefresh {
    private fun refreshAccess(client: OkHttpClient, refreshToken: String, userId: String) =
        CognitoControllerApi(client = client).refreshToken(refreshToken, userId).accessToken

    fun refreshTokens(client: OkHttpClient, refreshToken: String, userId: String): Tokens {
        val newAccess = refreshAccess(client, refreshToken, userId)
        return Tokens(newAccess, refreshToken, TokenParser().parseExpiration(newAccess))
    }
}