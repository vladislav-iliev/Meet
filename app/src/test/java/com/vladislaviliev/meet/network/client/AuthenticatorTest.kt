package com.vladislaviliev.meet.network.client

import com.vladislaviliev.meet.network.HEADER_AUTH_KEY
import com.vladislaviliev.meet.network.HEADER_AUTH_VALUE
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import okhttp3.Request
import okhttp3.Response
import org.junit.Test

class AuthenticatorTest {

    private val mockRenewToken: () -> String = mockk(relaxed = true)
    private val mockOnQuit: () -> Unit = mockk(relaxed = true)
    private val authenticator: Authenticator = Authenticator(mockRenewToken, mockOnQuit)

    private val baseRequest = Request.Builder().url("https://example.com").build()

    @Test
    fun `authenticate when first response is 401 and token renewed successfully should return new request`() {
        val newToken = "new_auth_token"

        val firstResponse = mockk<Response>()
        every { firstResponse.priorResponse } returns null
        every { firstResponse.request } returns baseRequest
        every { mockRenewToken.invoke() } returns newToken

        val result = authenticator.authenticate(null, firstResponse)

        verify(exactly = 1) { mockRenewToken.invoke() }
        verify(exactly = 0) { mockOnQuit.invoke() }

        assertNotNull(result)
        assertEquals(String.format(HEADER_AUTH_VALUE, newToken), result!!.header(HEADER_AUTH_KEY))
    }

    @Test
    fun `authenticate when first response is 401 and renewToken fails should call onQuit`() {
        val firstResponse = mockk<Response>()
        every { firstResponse.priorResponse } returns null
        every { firstResponse.request } returns baseRequest
        every { mockRenewToken.invoke() } throws RuntimeException()

        val result = authenticator.authenticate(null, firstResponse)

        verify(exactly = 1) { mockRenewToken.invoke() }
        verify(exactly = 1) { mockOnQuit.invoke() }
        assertNull(result)
    }

    @Test
    fun `authenticate when second consecutive response is 401 should call onQuit and not renew`() {
        val first401Response = mockk<Response>()
        val second401Response = mockk<Response>()

        every { first401Response.code } returns 401
        every { first401Response.priorResponse } returns null

        every { second401Response.priorResponse } returns first401Response
        every { second401Response.request } returns baseRequest

        val result = authenticator.authenticate(null, second401Response)

        verify(exactly = 1) { mockOnQuit.invoke() }
        verify(exactly = 0) { mockRenewToken.invoke() }
        assertNull(result)
    }

    @Test
    fun `authenticate when current is 401 and prior was not 401 should renew token`() {
        val priorNon401Response = mockk<Response>()
        val current401Response = mockk<Response>()
        val newToken = "new_token_after_non_401_prior"

        every { priorNon401Response.code } returns 500
        every { priorNon401Response.priorResponse } returns null

        every { current401Response.priorResponse } returns priorNon401Response
        every { current401Response.request } returns baseRequest
        every { current401Response.code } returns 401
        every { mockRenewToken.invoke() } returns newToken

        val result: Request? = authenticator.authenticate(null, current401Response)

        verify(exactly = 1) { mockRenewToken.invoke() }
        verify(exactly = 0) { mockOnQuit.invoke() }
        assertNotNull(result)
        assertEquals(String.format(HEADER_AUTH_VALUE, newToken), result!!.header(HEADER_AUTH_KEY))
    }
}
