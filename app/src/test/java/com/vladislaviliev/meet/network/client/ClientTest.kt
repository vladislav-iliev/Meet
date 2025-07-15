package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.Tokens
import io.mockk.mockk
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientTest {

    private val mockTokens: Tokens = mockk()
    private val mockRenewToken: () -> String = mockk()
    private val mockOnDisconnect: () -> Unit = mockk()

    @Test
    fun `Client instance creates OkHttpClient with HttpLoggingInterceptor`() {
        val okHttpClient = Client(mockTokens, mockRenewToken, mockOnDisconnect).instance

        val loggingInterceptor =
            okHttpClient.interceptors.firstOrNull { it is HttpLoggingInterceptor } as? HttpLoggingInterceptor

        assertNotNull(loggingInterceptor)
        assertEquals(HttpLoggingInterceptor.Level.BASIC, loggingInterceptor?.level)
    }

    @Test
    fun `Client instance creates OkHttpClient with AuthInterceptor`() {
        val okHttpClient = Client(mockTokens, mockRenewToken, mockOnDisconnect).instance
        assertNotNull("OkHttpClient instance should not be null", okHttpClient)
        assertNotNull(okHttpClient.interceptors.firstOrNull { it is AuthInterceptor })
    }

    @Test
    fun `OkHttpClient contains expected number of application interceptors`() {
        val okHttpClient = Client(mockTokens, mockRenewToken, mockOnDisconnect).instance
        assertEquals(2, okHttpClient.interceptors.size)
    }

    @Test
    fun `Client instance creates OkHttpClient with Authenticator`() {
        val okHttpClient = Client(mockTokens, mockRenewToken, mockOnDisconnect).instance
        assertNotNull(okHttpClient)
        assertTrue(okHttpClient.authenticator is Authenticator)
    }
}
