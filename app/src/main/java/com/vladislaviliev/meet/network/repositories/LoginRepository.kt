package com.vladislaviliev.meet.network.repositories

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.Tokens
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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

    private fun refreshSync(refreshToken: String, userId: String) {
        val response = runCatching { api.refreshToken(refreshToken, userId) }
        if (response.isFailure) {
            _tokens.value = Tokens.BLANK
            return
        }

        val responseValue = response.getOrNull()!!
        val newAccessToken = responseValue.accessToken
        val expiryParsed = runCatching { parser.parseExpiration(newAccessToken) }
        if (expiryParsed.isFailure) {
            _tokens.value = Tokens.BLANK
            return
        }

        val tokens = Tokens(userId, newAccessToken, refreshToken, expiryParsed.getOrNull()!!)
        _tokens.value = tokens
    }

    fun login(username: String, password: String) {
        scope.launch(dispatcher) {
            loginSync(username, password)
        }
    }

    fun refresh() {
        scope.launch(dispatcher) {
            refreshSync(tokens.value.refresh, tokens.value.userId)
        }
    }

    fun refreshSync() {
        refreshSync(tokens.value.refresh, tokens.value.userId)
    }

    fun logout() {
        scope.launch(dispatcher) {
            _tokens.value = Tokens.BLANK
        }
    }
}