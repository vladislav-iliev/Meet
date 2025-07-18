package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.HEADER_AUTH_KEY
import com.vladislaviliev.meet.network.HEADER_AUTH_VALUE
import com.vladislaviliev.meet.network.Tokens
import com.vladislaviliev.meet.network.repositories.login.LoginRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Test
import kotlin.test.assertNull

class AuthInterceptorTest {

    private val mockLoginRepository = mockk<LoginRepository>()
    private val authInterceptor = AuthInterceptor { mockLoginRepository }

    private companion object {
        const val TEST_USER_ID = "testUserId"
        const val TEST_ACCESS_TOKEN = "testAccessToken"
        const val TEST_REFRESH_TOKEN = "testRefreshToken"
        const val TEST_EXPIRATION_TIME = 1234567890L
    }

    @Test
    fun `intercept adds auth header when tokens are available`() {
        val tokensFlow =
            MutableStateFlow(Tokens(TEST_USER_ID, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME))
        every { mockLoginRepository.tokens } returns tokensFlow

        val mockChain = mockk<Interceptor.Chain>()
        every { mockChain.request() } returns Request.Builder().url("https://example.com").build()

        val mockResponse = mockk<Response>()
        val requestSlot = slot<Request>()
        every { mockChain.proceed(capture(requestSlot)) } returns mockResponse

        authInterceptor.intercept(mockChain)

        verify { mockChain.request() }
        verify(exactly = 1) { mockChain.proceed(any()) }

        val interceptedRequest = requestSlot.captured
        assertEquals(String.format(HEADER_AUTH_VALUE, TEST_ACCESS_TOKEN), interceptedRequest.header(HEADER_AUTH_KEY))
    }

    @Test
    fun `intercept doesn't add auth header when tokens are blank`() {
        val tokensFlow = MutableStateFlow(Tokens.BLANK)
        every { mockLoginRepository.tokens } returns tokensFlow

        val mockChain = mockk<Interceptor.Chain>()
        every { mockChain.request() } returns Request.Builder().url("https://example.com").build()

        val mockResponse = mockk<Response>()
        val requestSlot = slot<Request>()
        every { mockChain.proceed(capture(requestSlot)) } returns mockResponse

        authInterceptor.intercept(mockChain)

        verify { mockChain.request() }
        verify(exactly = 1) { mockChain.proceed(any()) }

        val interceptedRequest = requestSlot.captured
        assertNull(interceptedRequest.header(HEADER_AUTH_KEY))
    }

    @Test
    fun `intercept doesn't add auth header when repository is null`() {
        val authInterceptorWithNullRepo = AuthInterceptor { null }

        val mockChain = mockk<Interceptor.Chain>()
        every { mockChain.request() } returns Request.Builder().url("https://example.com").build()

        val mockResponse = mockk<Response>()
        val requestSlot = slot<Request>()
        every { mockChain.proceed(capture(requestSlot)) } returns mockResponse

        authInterceptorWithNullRepo.intercept(mockChain)

        verify { mockChain.request() }
        verify(exactly = 1) { mockChain.proceed(any()) }

        val interceptedRequest = requestSlot.captured
        assertNull(interceptedRequest.header(HEADER_AUTH_KEY))
    }

    @Test
    fun `intercept preserves existing headers`() {
        val tokensFlow =
            MutableStateFlow(Tokens(TEST_USER_ID, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME))
        every { mockLoginRepository.tokens } returns tokensFlow

        val mockChain = mockk<Interceptor.Chain>()
        val originalRequest = Request.Builder()
            .url("https://example.com")
            .header("Content-Type", "application/json")
            .header("Custom-Header", "custom-value")
            .build()
        every { mockChain.request() } returns originalRequest

        val mockResponse = mockk<Response>()
        val requestSlot = slot<Request>()
        every { mockChain.proceed(capture(requestSlot)) } returns mockResponse

        authInterceptor.intercept(mockChain)

        verify { mockChain.request() }
        verify(exactly = 1) { mockChain.proceed(any()) }

        val interceptedRequest = requestSlot.captured
        assertEquals(String.format(HEADER_AUTH_VALUE, TEST_ACCESS_TOKEN), interceptedRequest.header(HEADER_AUTH_KEY))
        assertEquals("application/json", interceptedRequest.header("Content-Type"))
        assertEquals("custom-value", interceptedRequest.header("Custom-Header"))
    }

    @Test
    fun `intercept returns chain response`() {
        val tokensFlow =
            MutableStateFlow(Tokens(TEST_USER_ID, TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN, TEST_EXPIRATION_TIME))
        every { mockLoginRepository.tokens } returns tokensFlow

        val mockChain = mockk<Interceptor.Chain>()
        every { mockChain.request() } returns Request.Builder().url("https://example.com").build()

        val mockResponse = mockk<Response>()
        every { mockChain.proceed(any()) } returns mockResponse

        val result = authInterceptor.intercept(mockChain)

        assertEquals(mockResponse, result)
    }
}
