package com.vladislaviliev.meet.network.requests

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.Tokens
import org.openapitools.client.apis.CognitoControllerApi

internal class RefreshTokenHandler(
    private val api: CognitoControllerApi,
    private val parser: TokenParser,
    private val onFinish: (Result<Tokens>) -> Unit
) {
    fun refreshTokens(refreshToken: String, userId: String) {
        val response = runCatching { api.refreshToken(refreshToken, userId) }
        if (response.isFailure) {
            onFinish(Result.failure(response.exceptionOrNull()!!))
            return
        }
        val responseValue = response.getOrNull()!!
        val newAccessToken = responseValue.accessToken
        val expiryParsed = runCatching { parser.parseExpiration(newAccessToken) }
        if (expiryParsed.isFailure) {
            onFinish(Result.failure(expiryParsed.exceptionOrNull()!!))
            return
        }
        val tokens = Tokens(userId, newAccessToken, refreshToken, expiryParsed.getOrNull()!!)
        onFinish(Result.success(tokens))
    }
}
