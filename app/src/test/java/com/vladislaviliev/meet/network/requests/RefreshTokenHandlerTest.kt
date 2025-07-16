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
import org.openapitools.client.models.RefreshTokenResponseDTO

class RefreshTokenHandlerTest {

    private val mockApi = mockk<CognitoControllerApi>()
    private val mockTokenParser = mockk<TokenParser>()

    @Test
    fun `refreshTokens success calls onFinish with success Result and correct Tokens`() {
        val refreshTokenString = "testRefreshToken"
        val userId = "testUserId"
        val newAccessToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjE3MzU2ODk2MDB9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val idTokenPlaceholder = "testIdToken"
        val expectedExpiration = 1735689600L
        val mockRefreshTokenResponse = RefreshTokenResponseDTO(idTokenPlaceholder, newAccessToken)


        every { mockApi.refreshToken(refreshTokenString, userId) } returns mockRefreshTokenResponse
        every { mockTokenParser.parseExpiration(newAccessToken) } returns expectedExpiration

        val resultSlot = slot<Result<Tokens>>()
        val refreshTokenHandler = RefreshTokenHandler(mockApi, mockTokenParser) { resultSlot.captured = it }

        refreshTokenHandler.refreshTokens(refreshTokenString, userId)

        assertTrue(resultSlot.isCaptured)
        val result = resultSlot.captured
        assertTrue(result.isSuccess)
        val tokens = result.getOrNull()!!
        assertEquals(newAccessToken, tokens.access)
        assertEquals(refreshTokenString, tokens.refresh)
        assertEquals(expectedExpiration, tokens.expiry)

        verify { mockApi.refreshToken(refreshTokenString, userId) }
        verify { mockTokenParser.parseExpiration(newAccessToken) }
    }

    @Test
    fun `refreshTokens failure on API call calls onFinish with failure Result`() {
        val refreshTokenString = "testRefreshToken"
        val userId = "testUserId"
        val expectedException = ClientException("API error", 500, null)

        every { mockApi.refreshToken(refreshTokenString, userId) } throws expectedException

        val resultSlot = slot<Result<Tokens>>()
        val refreshTokenHandler = RefreshTokenHandler(mockApi, mockTokenParser) { resultSlot.captured = it }

        refreshTokenHandler.refreshTokens(refreshTokenString, userId)

        assertTrue(resultSlot.isCaptured)
        val result = resultSlot.captured
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        assertNull(result.getOrNull())

        verify { mockApi.refreshToken(refreshTokenString, userId) }
        verify(exactly = 0) { mockTokenParser.parseExpiration(any()) }
    }

    @Test
    fun `refreshTokens failure on token parsing calls onFinish with failure Result`() {
        val refreshTokenString = "testRefreshToken"
        val userId = "testUserId"
        val newAccessToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val idTokenPlaceholder = "testIdToken"

        val mockRefreshTokenResponse = RefreshTokenResponseDTO(idTokenPlaceholder, newAccessToken)
        val expectedException = RuntimeException("Token parsing failed")

        every { mockApi.refreshToken(refreshTokenString, userId) } returns mockRefreshTokenResponse
        every { mockTokenParser.parseExpiration(newAccessToken) } throws expectedException

        val resultSlot = slot<Result<Tokens>>()
        val refreshTokenHandler = RefreshTokenHandler(mockApi, mockTokenParser) { resultSlot.captured = it }

        refreshTokenHandler.refreshTokens(refreshTokenString, userId)

        assertTrue(resultSlot.isCaptured)
        val result = resultSlot.captured
        assertTrue(result.isFailure)
        assertEquals(expectedException, result.exceptionOrNull())
        assertNull(result.getOrNull())

        verify { mockApi.refreshToken(refreshTokenString, userId) }
        verify { mockTokenParser.parseExpiration(newAccessToken) }
    }

    @Test
    fun `refreshTokens handles missing exp claim during parsing gracefully via TokenParser`() {
        val refreshTokenString = "testRefreshToken"
        val userId = "testUserId"
        val newAccessTokenWithoutExp =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
        val idTokenPlaceholder = "testIdToken"

        val mockRefreshTokenResponse = RefreshTokenResponseDTO(idTokenPlaceholder, newAccessTokenWithoutExp)
        val expectedExpirationFromParser = -1L

        every { mockApi.refreshToken(refreshTokenString, userId) } returns mockRefreshTokenResponse
        every { mockTokenParser.parseExpiration(newAccessTokenWithoutExp) } returns expectedExpirationFromParser

        val resultSlot = slot<Result<Tokens>>()
        val refreshTokenHandler = RefreshTokenHandler(mockApi, mockTokenParser) { resultSlot.captured = it }

        refreshTokenHandler.refreshTokens(refreshTokenString, userId)

        assertTrue(resultSlot.isCaptured)
        val result = resultSlot.captured
        assertTrue(result.isSuccess)
        val tokens = result.getOrNull()!!
        assertEquals(newAccessTokenWithoutExp, tokens.access)
        assertEquals(refreshTokenString, tokens.refresh)
        assertEquals(expectedExpirationFromParser, tokens.expiry)

        verify { mockApi.refreshToken(refreshTokenString, userId) }
        verify { mockTokenParser.parseExpiration(newAccessTokenWithoutExp) }
    }
}

