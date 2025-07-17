package com.vladislaviliev.meet.network.repositories

import com.vladislaviliev.meet.network.Tokens
import com.vladislaviliev.meet.user.User
import com.vladislaviliev.meet.user.UserState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.UserControllerApi

internal class UserRepository(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val loginTokens: StateFlow<Tokens>,
    private val api: UserControllerApi,
) {
    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState = _userState.asStateFlow()

    init {
        scope.launch { load() }
    }

    private suspend fun downloadInfo(): User {
        val response = withContext(dispatcher) { api.getUserInfo(loginTokens.value.userId) }
        return User(response.location.latitude, response.location.longitude)
    }

    private fun collectLoginTokens(user: User) {
        loginTokens
            .onEach { _userState.value = if (it.isBlank) UserState.Disconnected else UserState.Connected(user) }
            .launchIn(scope)
    }

    private suspend fun load() {
        val apiResponse = runCatching { downloadInfo() }
        if (apiResponse.isFailure) {
            _userState.value = UserState.Disconnected
            return
        }
        collectLoginTokens(apiResponse.getOrNull()!!)
    }
}