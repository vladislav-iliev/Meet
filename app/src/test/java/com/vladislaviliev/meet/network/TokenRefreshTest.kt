package com.vladislaviliev.meet.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TokenRefreshTest {

    private lateinit var spiedTokenRefresh: TokenRefresh

    @Before
    fun setUp() {
        spiedTokenRefresh = spyk(TokenRefresh())
    }

    @Test
    fun `refreshTokens returns new tokens with correct expiration when refreshRequest is mocked`() {
        val refreshTokenString = "testRefreshToken"
        val userId = "testUserId"
        val newAccessTokenFromMock =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE3MzU2ODk2MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val expectedExpiration = 1735689600L

        every {
            spiedTokenRefresh["refreshRequest"](any<OkHttpClient>(), any<String>(), any<String>())
        } returns newAccessTokenFromMock

        val result = spiedTokenRefresh.refreshTokens(mockk(), refreshTokenString, userId)

        assertEquals(newAccessTokenFromMock, result.access)
        assertEquals(refreshTokenString, result.refresh)
        assertEquals(expectedExpiration, result.expiration)
    }

    @Test
    fun `refreshTokens handles missing exp claim when refreshRequest is mocked`() {
        val refreshTokenString = "testRefreshToken"
        val userId = "testUserId"
        val newAccessTokenFromMock =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val expectedExpiration = -1L

        every {
            spiedTokenRefresh["refreshRequest"](any<OkHttpClient>(), any<String>(), any<String>())
        } returns newAccessTokenFromMock

        val result = spiedTokenRefresh.refreshTokens(mockk(), refreshTokenString, userId)

        assertEquals(newAccessTokenFromMock, result.access)
        assertEquals(refreshTokenString, result.refresh)
        assertEquals(expectedExpiration, result.expiration)
    }

    @Test
    fun `refreshTokens handles invalid JWT format when refreshRequest is mocked`() {
        val refreshTokenString = "testRefreshToken"
        val userId = "testUserId"
        val newAccessTokenFromMock = "invalid.jwt.token"
        val expectedExpiration = -1L

        every {
            spiedTokenRefresh["refreshRequest"](any<OkHttpClient>(), any<String>(), any<String>())
        } returns newAccessTokenFromMock

        val result = spiedTokenRefresh.refreshTokens(mockk(), refreshTokenString, userId)

        assertEquals(newAccessTokenFromMock, result.access)
        assertEquals(refreshTokenString, result.refresh)
        assertEquals(expectedExpiration, result.expiration)
    }
}
