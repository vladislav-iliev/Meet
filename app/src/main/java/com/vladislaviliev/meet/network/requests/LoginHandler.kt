package com.vladislaviliev.meet.network.requests

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.Tokens
import org.openapitools.client.apis.CognitoControllerApi

internal class LoginHandler(
    private val api: CognitoControllerApi,
    private val parser: TokenParser,
    private val onFinish: (Result<Tokens>) -> Unit
) {
    fun login(username: String, password: String) {
        val response = runCatching { api.login(username, password) }
        if (response.isFailure) {
            onFinish(Result.failure(response.exceptionOrNull()!!))
            return
        }
        val responseValue = response.getOrNull()!!
        val accessToken = responseValue.accessToken
        val expiryParsed = runCatching { parser.parseExpiration(accessToken) }
        if (expiryParsed.isFailure) {
            onFinish(Result.failure(expiryParsed.exceptionOrNull()!!))
            return
        }
        val tokens = Tokens(responseValue.userId, accessToken, responseValue.refreshToken, expiryParsed.getOrNull()!!)
        onFinish(Result.success(tokens))
    }
}