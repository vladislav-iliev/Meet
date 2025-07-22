package com.vladislaviliev.meet.network.repositories.login

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.Tokens
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.CognitoControllerApi

internal class LoginRepository(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val api: CognitoControllerApi,
    private val parser: TokenParser
) {
    private val _tokens = MutableStateFlow(Tokens.BLANK)
    val tokens = _tokens.asStateFlow()

    private fun loginSync(username: String, password: String) {
        val response = runCatching { api.login(username, password) }
        if (response.isFailure) {
            _tokens.value = Tokens.BLANK
            return
        }

        val responseValue = response.getOrNull()!!
        val accessToken = responseValue.accessToken
        val expiryParsed = runCatching { parser.parseExpiration(accessToken) }
        if (expiryParsed.isFailure) {
            _tokens.value = Tokens.BLANK
            return
        }

        val tokens = Tokens(responseValue.userId, accessToken, responseValue.refreshToken, expiryParsed.getOrNull()!!)
        _tokens.value = tokens
    }

    suspend fun loginDispatched(username: String, password: String) =
        withContext(dispatcher) { loginSync(username, password) }

    fun refreshSync(): Result<Unit> {
        val refreshToken = tokens.value.refresh
        val userId = tokens.value.userId

        val response = runCatching { api.refreshToken(refreshToken, userId) }
        if (response.isFailure) {
            _tokens.value = Tokens.BLANK
            return Result.failure(response.exceptionOrNull()!!)
        }

        val responseValue = response.getOrNull()!!
        val newAccessToken = responseValue.accessToken
        val expiryParsed = runCatching { parser.parseExpiration(newAccessToken) }
        if (expiryParsed.isFailure) {
            _tokens.value = Tokens.BLANK
            return Result.failure(expiryParsed.exceptionOrNull()!!)
        }

        val tokens = Tokens(userId, newAccessToken, refreshToken, expiryParsed.getOrNull()!!)
        _tokens.value = tokens
        return Result.success(Unit)
    }

    suspend fun refreshDispatched() = withContext(dispatcher) { refreshSync() }

    fun clear() {
        _tokens.value = Tokens.BLANK
    }
}