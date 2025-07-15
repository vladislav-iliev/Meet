package com.vladislaviliev.meet.network

import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class AuthSignTest {

    @Test
    fun `sign adds Authorization header correctly`() {
        val originalRequest =
            Request.Builder().url("https://example.com").header("Existing-Header", "ExistingValue").build()
        val testToken = "sample_access_token"

        val signedRequest = originalRequest.sign(testToken)

        val expectedAuthHeaderValue = String.format(HEADER_AUTH_VALUE, testToken)
        assertEquals(expectedAuthHeaderValue, signedRequest.header(HEADER_AUTH_KEY))
    }

    @Test
    fun `sign preserves existing headers`() {
        val originalRequest =
            Request.Builder().url("https://example.com").header("Existing-Header", "ExistingValue").build()
        val testToken = "sample_access_token"

        val signedRequest = originalRequest.sign(testToken)

        assertEquals("ExistingValue", signedRequest.header("Existing-Header"))
    }

    @Test
    fun `sign works with request that has no existing headers`() {
        val originalRequest = Request.Builder().url("https://example.com").build()
        val testToken = "another_token"

        val signedRequest = originalRequest.sign(testToken)

        val expectedAuthHeaderValue = String.format(HEADER_AUTH_VALUE, testToken)
        assertEquals(expectedAuthHeaderValue, signedRequest.header(HEADER_AUTH_KEY))
        assertEquals(1, signedRequest.headers.size)
    }

    @Test
    fun `sign does not modify the original request`() {
        val originalRequest = Request.Builder().url("https://example.com").build()
        val testToken = "immutable_token"

        val signedRequest = originalRequest.sign(testToken)

        assertEquals(null, originalRequest.header(HEADER_AUTH_KEY))
        assertEquals(0, originalRequest.headers.size)
        assertNotSame(originalRequest, signedRequest)
    }
}
