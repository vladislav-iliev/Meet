package com.vladislaviliev.meet.network.requests

import com.vladislaviliev.meet.network.Tokens
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openapitools.client.infrastructure.ClientException

class LoginHandlerTest {

    private val mockClient = mockk<OkHttpClient>()
    private val loginHandler = LoginHandler()

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `login success calls onFinish with new tokens and returns success`() {
        val username = "testUser"
        val password = "testPassword"
        val expectedTokens = Tokens("newAccess", "newRefresh", 123L)

        val tokensArgumentSlot = slot<Tokens>()

        mockkConstructor(Login::class)
        every { anyConstructed<Login>().login(mockClient, username, password) } returns expectedTokens

        val result = loginHandler.login(mockClient, username, password) { tokens ->
            tokensArgumentSlot.captured = tokens
        }

        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())

        verify { anyConstructed<Login>().login(mockClient, username, password) }

        assertTrue(tokensArgumentSlot.isCaptured)
        assertEquals(expectedTokens, tokensArgumentSlot.captured)
    }

    @Test
    fun `login failure calls onFinish with BLANK tokens and returns failure`() {
        val username = "testUser"
        val password = "testPassword"
        val expectedException = ClientException("Client error : 400 ", 400, null)
        val tokensArgumentSlot = slot<Tokens>()

        mockkConstructor(Login::class)
        every { anyConstructed<Login>().login(mockClient, username, password) } throws expectedException

        val result = loginHandler.login(mockClient, username, password) { tokens ->
            tokensArgumentSlot.captured = tokens
        }

        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        verify { anyConstructed<Login>().login(mockClient, username, password) }
        assertTrue(tokensArgumentSlot.isCaptured)
        assertEquals(Tokens.BLANK, tokensArgumentSlot.captured)
    }
}
