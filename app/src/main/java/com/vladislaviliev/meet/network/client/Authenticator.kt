package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.repositories.LoginRepository
import com.vladislaviliev.meet.network.sign
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

internal class Authenticator(
    private val loginRepository: LoginRepository,
    private val onQuit: () -> Unit,
) : Authenticator {

    private fun priorResponsesCount(response: Response) = generateSequence(response) { it.priorResponse }.count()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (1 < priorResponsesCount(response) && 401 == response.priorResponse?.code) { // quit after 2 401s: 1 token expiry + 1 failed refresh)
            onQuit()
            return null
        }
        loginRepository.refreshSync()
        val newTokens = loginRepository.tokens.value
        if (newTokens.isBlank) {
            onQuit()
            return null
        }
        return response.request.sign(newTokens.access)
    }
}