package com.vladislaviliev.meet.network.requests

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.Tokens
import okhttp3.OkHttpClient
import org.openapitools.client.apis.CognitoControllerApi

internal class Login {

    private fun serverResponse(client: OkHttpClient, username: String, password: String) =
        CognitoControllerApi(client = client).login(username, password)

    fun login(client: OkHttpClient, username: String, password: String): Tokens {
        val response = serverResponse(client, username, password)
        val accessToken = response.accessToken
        return Tokens(accessToken, response.refreshToken, TokenParser().parseExpiration(accessToken))
    }
}