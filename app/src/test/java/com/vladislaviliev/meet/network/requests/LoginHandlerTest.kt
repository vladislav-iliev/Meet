package com.vladislaviliev.meet.network.requests

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.Tokens
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openapitools.client.apis.CognitoControllerApi
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.LoginResponseDTO

class LoginHandlerTest {

    private val mockApi = mockk<CognitoControllerApi>()
    private val mockTokenParser = mockk<TokenParser>()

    @Test
    fun `login success calls onFinish with success Result and correct Tokens`() {
        val username = "testUser"
        val password = "testPassword"
        val accessToken = "testAccessToken"
        val refreshToken = "testRefreshToken"
        val expirationTime = 1234567890L
        val mockLoginResponse = LoginResponseDTO(accessToken, refreshToken, "testUserId")

        every { mockApi.login(username, password) } returns mockLoginResponse
        every { mockTokenParser.parseExpiration(accessToken) } returns expirationTime

        val resultSlot = slot<Result<Tokens>>()
        val loginHandler = LoginHandler(mockApi, mockTokenParser) { result -> resultSlot.captured = result }

        loginHandler.login(username, password)

        assertTrue(resultSlot.isCaptured)
        val result = resultSlot.captured
        assertTrue(result.isSuccess)
        val tokens = result.getOrNull()
        assertEquals(accessToken, tokens?.access)
        assertEquals(refreshToken, tokens?.refresh)
        assertEquals(expirationTime, tokens?.expiration)

        verify { mockApi.login(username, password) }
        verify { mockTokenParser.parseExpiration(accessToken) }
    }

    @Test
    fun `login failure calls onFinish with failure Result`() {
        val username = "testUser"
        val password = "testPassword"
        val expectedException = ClientException("Client error", 400, null)

        every { mockApi.login(username, password) } throws expectedException

        val resultSlot = slot<Result<Tokens>>()
        val loginHandler = LoginHandler(mockApi, mockTokenParser) { result -> resultSlot.captured = result }

        loginHandler.login(username, password)

        assertTrue(resultSlot.isCaptured)
        val result = resultSlot.captured
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        assertNull(result.getOrNull())

        verify { mockApi.login(username, password) }
    }

    @Test
    fun `login failure during token parsing calls onFinish with failure Result`() {
        val username = "testUser"
        val password = "testPassword"
        val accessToken = "testAccessToken"
        val refreshToken = "testRefreshToken"
        val mockLoginResponse = LoginResponseDTO(accessToken, refreshToken, "testUserId")
        val expectedException = RuntimeException("Token parsing failed")

        every { mockApi.login(username, password) } returns mockLoginResponse
        every { mockTokenParser.parseExpiration(accessToken) } throws expectedException

        val resultSlot = slot<Result<Tokens>>()
        val loginHandler = LoginHandler(mockApi, mockTokenParser) { result -> resultSlot.captured = result }

        loginHandler.login(username, password)

        assertTrue(resultSlot.isCaptured)
        val result = resultSlot.captured
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        assertNull(result.getOrNull())

        verify { mockApi.login(username, password) }
        verify { mockTokenParser.parseExpiration(accessToken) }
    }
}
