package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.Tokens
import com.vladislaviliev.meet.network.repositories.login.LoginRepository
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryProvider
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ClientIntegrationTest {

    private val loginRepositoryProvider = LoginRepositoryProvider()
    private val mockChain = mockk<Interceptor.Chain>()
    private val mockRoute = mockk<Route>()

    private fun createMockLoginRepository(tokens: Tokens) = mockk<LoginRepository> {
        every { this@mockk.tokens.value } returns tokens
        every { refreshSync() } just Runs
    }

    @Test
    fun `test AuthInterceptor receives updated LoginRepository values`() = runTest {
        val futureExpiry = System.currentTimeMillis() + 3600000 // 1 hour from now
        val initialTokens = Tokens("user1", "initial_access_token", "refresh_token", futureExpiry)
        val updatedTokens = Tokens("user2", "updated_access_token", "refresh_token", futureExpiry)

        val initialRepository = createMockLoginRepository(initialTokens)
        val updatedRepository = createMockLoginRepository(updatedTokens)

        val authInterceptor = AuthInterceptor { loginRepositoryProvider.current.value }

        val request = Request.Builder().url("https://example.com/test").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns response

        // Test with initial repository
        loginRepositoryProvider.update(initialRepository)
        authInterceptor.intercept(mockChain)

        val capturedRequest1 = slot<Request>()
        verify { mockChain.proceed(capture(capturedRequest1)) }
        assertEquals("Bearer initial_access_token", capturedRequest1.captured.header("Authorization"))

        // Test with updated repository
        clearMocks(mockChain)
        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns response

        loginRepositoryProvider.update(updatedRepository)
        authInterceptor.intercept(mockChain)

        val capturedRequest2 = slot<Request>()
        verify { mockChain.proceed(capture(capturedRequest2)) }
        assertEquals("Bearer updated_access_token", capturedRequest2.captured.header("Authorization"))
    }

    @Test
    fun `test Authenticator receives updated LoginRepository values`() = runTest {
        val expiry = 123456L
        val initialTokens = Tokens("user1", "initial_access_token", "refresh_token", expiry)
        val updatedTokens = Tokens("user2", "updated_access_token", "refresh_token", expiry)

        val initialRepository = createMockLoginRepository(initialTokens)
        val updatedRepository = createMockLoginRepository(updatedTokens)

        val authenticator = Authenticator(loginRepositoryProvider.current::value)

        val originalRequest = Request.Builder()
            .url("https://example.com/test")
            .build()

        val unauthorizedResponse = Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        // Test with initial repository
        loginRepositoryProvider.update(initialRepository)
        val authenticatedRequest1 = authenticator.authenticate(mockRoute, unauthorizedResponse)

        assertNotNull(authenticatedRequest1)
        assertEquals("Bearer initial_access_token", authenticatedRequest1.header("Authorization"))
        verify { initialRepository.refreshSync() }

        // Update to new repository with different tokens
        loginRepositoryProvider.update(updatedRepository)
        val authenticatedRequest2 = authenticator.authenticate(mockRoute, unauthorizedResponse)

        assertNotNull(authenticatedRequest2)
        assertEquals("Bearer updated_access_token", authenticatedRequest2.header("Authorization"))
        verify { updatedRepository.refreshSync() }
    }

    @Test
    fun `test AuthInterceptor handles null LoginRepository`() = runTest {
        loginRepositoryProvider.update(null)

        val authInterceptor = AuthInterceptor { loginRepositoryProvider.current.value }

        val request = Request.Builder().url("https://example.com/test").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns response

        authInterceptor.intercept(mockChain)

        val capturedRequest = slot<Request>()
        verify { mockChain.proceed(capture(capturedRequest)) }
        assertNull(capturedRequest.captured.header("Authorization"))
    }

    @Test
    fun `test Authenticator handles null LoginRepository`() = runTest {
        loginRepositoryProvider.update(null)

        val authenticator = Authenticator(loginRepositoryProvider.current::value)

        val originalRequest = Request.Builder()
            .url("https://example.com/test")
            .build()

        val unauthorizedResponse = Response.Builder()
            .request(originalRequest)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .build()

        val authenticatedRequest = authenticator.authenticate(mockRoute, unauthorizedResponse)

        assertNull(authenticatedRequest)
    }

    @Test
    fun `test repository switching between interceptor calls`() = runTest {
        val futureExpiry = System.currentTimeMillis() + 3600000 // 1 hour from now
        val repo1Tokens = Tokens("user1", "token1", "refresh1", futureExpiry)
        val repo2Tokens = Tokens("user2", "token2", "refresh2", futureExpiry)

        val repository1 = createMockLoginRepository(repo1Tokens)
        val repository2 = createMockLoginRepository(repo2Tokens)

        val authInterceptor = AuthInterceptor { loginRepositoryProvider.current.value }

        val request = Request.Builder().url("https://example.com/test").build()
        val response = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .build()

        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns response

        //  Test with repository1
        loginRepositoryProvider.update(repository1)
        authInterceptor.intercept(mockChain)

        val capturedRequest1 = slot<Request>()
        verify { mockChain.proceed(capture(capturedRequest1)) }
        assertEquals("Bearer token1", capturedRequest1.captured.header("Authorization"))

        // Switch to repository2
        clearMocks(mockChain)
        every { mockChain.request() } returns request
        every { mockChain.proceed(any()) } returns response

        loginRepositoryProvider.update(repository2)
        authInterceptor.intercept(mockChain)

        val capturedRequest2 = slot<Request>()
        verify { mockChain.proceed(capture(capturedRequest2)) }
        assertEquals("Bearer token2", capturedRequest2.captured.header("Authorization"))
    }
}