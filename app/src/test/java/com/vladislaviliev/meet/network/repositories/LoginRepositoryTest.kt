package com.vladislaviliev.meet.network.repositories

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.Tokens
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.openapitools.client.apis.CognitoControllerApi
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.LoginResponseDTO
import org.openapitools.client.models.RefreshTokenResponseDTO

@OptIn(ExperimentalCoroutinesApi::class)
class LoginRepositoryTest {

    private val mockApi = mockk<CognitoControllerApi>()
    private val mockTokenParser = mockk<TokenParser>()

    private companion object {
        const val TEST_USERNAME = "testUser"
        const val TEST_PASSWORD = "testPassword"
        const val TEST_USER_ID = "testUserId"
        const val TEST_ACCESS_TOKEN = "testAccessToken"
        const val TEST_REFRESH_TOKEN = "testRefreshToken"
        const val TEST_EXPIRATION_TIME = 1234567890L
        const val NEW_ACCESS_TOKEN = "newAccessToken"
        const val NEW_EXPIRATION_TIME = 9876543210L
    }

    private fun TestScope.createRepository() =
        LoginRepository(this, coroutineContext[CoroutineDispatcher]!!, mockApi, mockTokenParser)

    private fun TestScope.performInitialLogin(repository: LoginRepository) {
        val mockLoginResponse = LoginResponseDTO(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_USER_ID)
        coEvery { mockApi.login(any(), any()) } returns mockLoginResponse
        every { mockTokenParser.parseExpiration(TEST_ACCESS_TOKEN) } returns TEST_EXPIRATION_TIME
        repository.login(TEST_USERNAME, TEST_PASSWORD)
        advanceUntilIdle()
    }

    @Test
    fun `login success emits correct tokens to StateFlow`() = runTest {
        val repository = createRepository()
        val mockLoginResponse = LoginResponseDTO(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_USER_ID)

        coEvery { mockApi.login(TEST_USERNAME, TEST_PASSWORD) } returns mockLoginResponse
        every { mockTokenParser.parseExpiration(TEST_ACCESS_TOKEN) } returns TEST_EXPIRATION_TIME

        repository.login(TEST_USERNAME, TEST_PASSWORD)
        advanceUntilIdle()

        val expectedTokens = Tokens(TEST_USER_ID, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME)
        assertEquals(expectedTokens, repository.tokens.value)

        coVerify { mockApi.login(TEST_USERNAME, TEST_PASSWORD) }
        verify { mockTokenParser.parseExpiration(TEST_ACCESS_TOKEN) }
    }

    @Test
    fun `login failure emits blank tokens to StateFlow`() = runTest {
        val repository = createRepository()
        val expectedException = ClientException("Client error", 400, null)

        coEvery { mockApi.login(TEST_USERNAME, TEST_PASSWORD) } throws expectedException

        repository.login(TEST_USERNAME, TEST_PASSWORD)
        advanceUntilIdle()

        assertEquals(Tokens.BLANK, repository.tokens.value)

        coVerify { mockApi.login(TEST_USERNAME, TEST_PASSWORD) }
        verify(exactly = 0) { mockTokenParser.parseExpiration(any()) }
    }

    @Test
    fun `login failure during token parsing emits blank tokens to StateFlow`() = runTest {
        val repository = createRepository()
        val mockLoginResponse = LoginResponseDTO(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_USER_ID)
        val expectedException = RuntimeException("Token parsing failed")

        coEvery { mockApi.login(TEST_USERNAME, TEST_PASSWORD) } returns mockLoginResponse
        every { mockTokenParser.parseExpiration(TEST_ACCESS_TOKEN) } throws expectedException

        repository.login(TEST_USERNAME, TEST_PASSWORD)
        advanceUntilIdle()

        assertEquals(Tokens.BLANK, repository.tokens.value)

        coVerify { mockApi.login(TEST_USERNAME, TEST_PASSWORD) }
        verify { mockTokenParser.parseExpiration(TEST_ACCESS_TOKEN) }
    }

    @Test
    fun `refresh success emits updated tokens to StateFlow`() = runTest {
        val repository = createRepository()

        performInitialLogin(repository)

        val mockRefreshResponse = RefreshTokenResponseDTO("idToken", NEW_ACCESS_TOKEN)
        coEvery { mockApi.refreshToken(TEST_REFRESH_TOKEN, TEST_USER_ID) } returns mockRefreshResponse
        every { mockTokenParser.parseExpiration(NEW_ACCESS_TOKEN) } returns NEW_EXPIRATION_TIME

        repository.refresh()
        advanceUntilIdle()

        val expectedTokens = Tokens(TEST_USER_ID, NEW_ACCESS_TOKEN, TEST_REFRESH_TOKEN, NEW_EXPIRATION_TIME)
        assertEquals(expectedTokens, repository.tokens.value)

        coVerify { mockApi.refreshToken(TEST_REFRESH_TOKEN, TEST_USER_ID) }
        verify { mockTokenParser.parseExpiration(NEW_ACCESS_TOKEN) }
    }

    @Test
    fun `refresh with blank tokens tries to refresh with blank values`() = runTest {
        val repository = createRepository()

        assertEquals(Tokens.BLANK, repository.tokens.value)

        coEvery { mockApi.refreshToken(Tokens.BLANK.refresh, Tokens.BLANK.userId) } throws
                ClientException("Invalid token", 400, null)

        repository.refresh()
        advanceUntilIdle()

        assertEquals(Tokens.BLANK, repository.tokens.value)

        coVerify { mockApi.refreshToken(Tokens.BLANK.refresh, Tokens.BLANK.userId) }
        verify(exactly = 0) { mockTokenParser.parseExpiration(any()) }
    }

    @Test
    fun `refresh failure on API call emits blank tokens to StateFlow`() = runTest {
        val repository = createRepository()

        performInitialLogin(repository)
        clearMocks(mockTokenParser, answers = false)

        val expectedException = ClientException("API error", 500, null)
        coEvery { mockApi.refreshToken(TEST_REFRESH_TOKEN, TEST_USER_ID) } throws expectedException

        repository.refresh()
        advanceUntilIdle()

        assertEquals(Tokens.BLANK, repository.tokens.value)

        coVerify { mockApi.refreshToken(TEST_REFRESH_TOKEN, TEST_USER_ID) }
        verify(exactly = 0) { mockTokenParser.parseExpiration(any()) }
    }

    @Test
    fun `refresh failure on token parsing emits blank tokens to StateFlow`() = runTest {
        val repository = createRepository()

        performInitialLogin(repository)

        val mockRefreshResponse = RefreshTokenResponseDTO("idToken", NEW_ACCESS_TOKEN)
        val expectedException = RuntimeException("Token parsing failed")

        coEvery { mockApi.refreshToken(TEST_REFRESH_TOKEN, TEST_USER_ID) } returns mockRefreshResponse
        every { mockTokenParser.parseExpiration(NEW_ACCESS_TOKEN) } throws expectedException

        repository.refresh()
        advanceUntilIdle()

        assertEquals(Tokens.BLANK, repository.tokens.value)

        coVerify { mockApi.refreshToken(TEST_REFRESH_TOKEN, TEST_USER_ID) }
        verify { mockTokenParser.parseExpiration(NEW_ACCESS_TOKEN) }
    }

    @Test
    fun `logout emits blank tokens to StateFlow`() = runTest {
        val repository = createRepository()

        performInitialLogin(repository)

        val expectedTokens = Tokens(TEST_USER_ID, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME)
        assertEquals(expectedTokens, repository.tokens.value)

        repository.logout()
        advanceUntilIdle()

        assertEquals(Tokens.BLANK, repository.tokens.value)
    }

    @Test
    fun `tokens StateFlow starts with blank tokens`() = runTest {
        val repository = createRepository()
        assertEquals(Tokens.BLANK, repository.tokens.value)
    }
}