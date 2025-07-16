package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.HEADER_AUTH_KEY
import com.vladislaviliev.meet.network.HEADER_AUTH_VALUE
import com.vladislaviliev.meet.network.Tokens
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.junit.Test

class AuthInterceptorTest {
    @Test
    fun `test intercept`() {
        val testToken = "test_access_token"

        val mockChain = mockk<Interceptor.Chain>()
        val originalRequest = Request.Builder().url("https://example.com").build()
        every { mockChain.request() } returns originalRequest

        val mockResponse = mockk<Response>()
        val requestSlot = slot<Request>()
        every { mockChain.proceed(capture(requestSlot)) } returns mockResponse

        AuthInterceptor(Tokens("", testToken, "", 0L)).intercept(mockChain)

        verify { mockChain.request() }

        verify(exactly = 1) { mockChain.proceed(any()) }

        val interceptedRequest = requestSlot.captured
        assertEquals(String.format(HEADER_AUTH_VALUE, testToken), interceptedRequest.header(HEADER_AUTH_KEY))
    }
}
