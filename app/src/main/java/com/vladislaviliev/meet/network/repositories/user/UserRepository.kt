package com.vladislaviliev.meet.network.repositories.user

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.UserControllerApi

internal class UserRepository(
    private val dispatcher: CoroutineDispatcher, private val api: UserControllerApi, val userId: String
) {
    private val _user = MutableStateFlow(User.BLANK)
    val user = _user.asStateFlow()

    private suspend fun downloadUser() = withContext(dispatcher) {
        runCatching {
            val location = api.getUserInfo(userId).location
            User(location.latitude, location.longitude)
        }
    }

    suspend fun download(): Result<Unit> {
        val apiResponse = downloadUser()
        if (apiResponse.isFailure) {
            _user.value = User.BLANK
            return Result.failure(apiResponse.exceptionOrNull()!!)
        }
        _user.value = apiResponse.getOrNull()!!
        return Result.success(Unit)
    }
}