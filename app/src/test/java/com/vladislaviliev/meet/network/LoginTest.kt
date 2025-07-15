package com.vladislaviliev.meet.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.spyk
import io.mockk.unmockkAll
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Test
import org.openapitools.client.models.LoginResponseDTO

class LoginTest {

    private val login = spyk<Login>()
    private val mockClient = mockk<OkHttpClient>()

    @Test
    fun `login returns Tokens on successful server response`() {
        val username = "testUser"
        val password = "testPassword"
        val accessToken = "testAccessToken"
        val refreshToken = "testRefreshToken"
        val expirationTime = 1234567890L

        mockkConstructor(TokenParser::class)
        val mockLoginResponse = LoginResponseDTO(accessToken, refreshToken, "testUserId")

        every { login["serverResponse"](mockClient, username, password) } returns mockLoginResponse
        every { anyConstructed<TokenParser>().parseExpiration(accessToken) } returns expirationTime

        val expectedTokens = Tokens(accessToken, refreshToken, expirationTime)
        val result = login.login(mockClient, username, password)

        assertEquals(expectedTokens.access, result.access)
        assertEquals(expectedTokens.refresh, result.refresh)
        assertEquals(expectedTokens.expiration, result.expiration)
        unmockkAll()
    }

    @Test(expected = RuntimeException::class)
    fun `login throws exception when serverResponse fails`() {
        val username = "testUser"
        val password = "testPassword"
        every { login["serverResponse"](mockClient, username, password) } throws RuntimeException()
        login.login(mockClient, username, password)
    }
}
