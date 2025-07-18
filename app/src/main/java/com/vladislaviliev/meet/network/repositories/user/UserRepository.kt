package com.vladislaviliev.meet.network.repositories.user

import com.vladislaviliev.meet.network.Tokens
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.UserControllerApi

internal class UserRepository(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val api: UserControllerApi,
    val loginTokens: StateFlow<Tokens>,
) {
    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    init {
        load()
    }

    private suspend fun downloadInfo(): User {
        val response = withContext(dispatcher) { api.getUserInfo(loginTokens.value.userId) }
        return User(response.location.latitude, response.location.longitude)
    }

    private fun load() = scope.launch {
        val apiResponse = runCatching { downloadInfo() }
        _user.value = apiResponse.getOrNull()
    }
}