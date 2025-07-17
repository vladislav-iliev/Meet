package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.HEADER_AUTH_KEY
import com.vladislaviliev.meet.network.HEADER_AUTH_VALUE
import com.vladislaviliev.meet.network.Tokens
import com.vladislaviliev.meet.network.repositories.LoginRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Request
import okhttp3.Response
import org.junit.Test

class AuthenticatorTest {

    private val mockLoginRepository = mockk<LoginRepository>(relaxed = true)
    private val mockOnQuit = mockk<() -> Unit>(relaxed = true)
    private val authenticator = Authenticator(mockLoginRepository, mockOnQuit)

    private val baseRequest = Request.Builder().url("https://example.com").build()

    private companion object {
        const val TEST_USER_ID = "testUserId"
        const val TEST_ACCESS_TOKEN = "testAccessToken"
        const val TEST_REFRESH_TOKEN = "testRefreshToken"
        const val TEST_EXPIRATION_TIME = 1234567890L
        const val NEW_ACCESS_TOKEN = "newAccessToken"
    }

    @Test
    fun `authenticate when first response is 401 and refresh succeeds should return new request`() {
        val tokensFlow =
            MutableStateFlow(Tokens(TEST_USER_ID, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME))
        val refreshedTokens = Tokens(TEST_USER_ID, NEW_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME)

        every { mockLoginRepository.tokens } returns tokensFlow
        coEvery { mockLoginRepository.refreshSync() } coAnswers { tokensFlow.value = refreshedTokens }

        val firstResponse = mockk<Response>()
        every { firstResponse.priorResponse } returns null
        every { firstResponse.request } returns baseRequest

        val result = authenticator.authenticate(null, firstResponse)

        coVerify(exactly = 1) { mockLoginRepository.refreshSync() }
        verify(exactly = 0) { mockOnQuit.invoke() }

        assertNotNull(result)
        assertEquals(String.format(HEADER_AUTH_VALUE, NEW_ACCESS_TOKEN), result!!.header(HEADER_AUTH_KEY))
    }

    @Test
    fun `authenticate when first response is 401 and refresh fails should call onQuit`() {
        val tokensFlow =
            MutableStateFlow(Tokens(TEST_USER_ID, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME))

        every { mockLoginRepository.tokens } returns tokensFlow
        coEvery { mockLoginRepository.refreshSync() } coAnswers { tokensFlow.value = Tokens.BLANK }

        val firstResponse = mockk<Response>()
        every { firstResponse.priorResponse } returns null
        every { firstResponse.request } returns baseRequest

        val result = authenticator.authenticate(null, firstResponse)

        coVerify(exactly = 1) { mockLoginRepository.refreshSync() }
        verify(exactly = 1) { mockOnQuit.invoke() }
        assertNull(result)
    }

    @Test
    fun `authenticate when second consecutive response is 401 should call onQuit and not refresh`() {
        val first401Response = mockk<Response>()
        val second401Response = mockk<Response>()

        every { first401Response.code } returns 401
        every { first401Response.priorResponse } returns null

        every { second401Response.priorResponse } returns first401Response
        every { second401Response.request } returns baseRequest

        val result = authenticator.authenticate(null, second401Response)

        verify(exactly = 1) { mockOnQuit.invoke() }
        coVerify(exactly = 0) { mockLoginRepository.refreshSync() }
        assertNull(result)
    }

    @Test
    fun `authenticate when current is 401 and prior was not 401 should refresh token`() {
        val tokensFlow =
            MutableStateFlow(Tokens(TEST_USER_ID, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME))
        val refreshedTokens = Tokens(TEST_USER_ID, NEW_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME)

        every { mockLoginRepository.tokens } returns tokensFlow
        coEvery { mockLoginRepository.refreshSync() } coAnswers { tokensFlow.value = refreshedTokens }

        val priorNon401Response = mockk<Response>()
        val current401Response = mockk<Response>()

        every { priorNon401Response.code } returns 500
        every { priorNon401Response.priorResponse } returns null

        every { current401Response.priorResponse } returns priorNon401Response
        every { current401Response.request } returns baseRequest
        every { current401Response.code } returns 401

        val result = authenticator.authenticate(null, current401Response)

        coVerify(exactly = 1) { mockLoginRepository.refreshSync() }
        verify(exactly = 0) { mockOnQuit.invoke() }
        assertNotNull(result)
        assertEquals(String.format(HEADER_AUTH_VALUE, NEW_ACCESS_TOKEN), result!!.header(HEADER_AUTH_KEY))
    }
}
