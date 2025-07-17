package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.repositories.LoginRepository
import io.mockk.mockk
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClientTest {

    private val mockLoginRepository = mockk<LoginRepository>()
    private val mockOnDisconnect = mockk<() -> Unit>()
    private val okHttpClient = Client(mockLoginRepository, mockOnDisconnect).instance

    @Test
    fun `Client instance creates OkHttpClient with HttpLoggingInterceptor`() {
        val loggingInterceptor =
            okHttpClient.interceptors.firstOrNull { it is HttpLoggingInterceptor } as? HttpLoggingInterceptor

        assertNotNull(loggingInterceptor)
        assertEquals(HttpLoggingInterceptor.Level.BASIC, loggingInterceptor?.level)
    }

    @Test
    fun `Client instance creates OkHttpClient with AuthInterceptor`() {
        assertNotNull(okHttpClient.interceptors.firstOrNull { it is AuthInterceptor })
    }

    @Test
    fun `OkHttpClient contains expected number of application interceptors`() {
        assertEquals(2, okHttpClient.interceptors.size)
    }

    @Test
    fun `Client instance creates OkHttpClient with Authenticator`() {
        assertTrue(okHttpClient.authenticator is Authenticator)
    }

    @Test
    fun `Client instance is properly configured`() {
        val interceptors = okHttpClient.interceptors
        assertTrue(interceptors[0] is HttpLoggingInterceptor)
        assertTrue(interceptors[1] is AuthInterceptor)
        assertTrue(okHttpClient.authenticator is Authenticator)
    }
}
