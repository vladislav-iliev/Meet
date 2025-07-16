package com.vladislaviliev.meet.network.requests

import com.vladislaviliev.meet.network.Tokens
import okhttp3.OkHttpClient

internal class LoginHandler {
    fun login(client: OkHttpClient, username: String, password: String, onFinish: (Tokens) -> Unit): Result<Unit> {
        val response = runCatching { Login().login(client, username, password) }
        if (response.isFailure) {
            onFinish(Tokens.BLANK)
            return Result.failure(response.exceptionOrNull()!!)
        }
        onFinish(response.getOrNull()!!)
        return Result.success(Unit)
    }
}