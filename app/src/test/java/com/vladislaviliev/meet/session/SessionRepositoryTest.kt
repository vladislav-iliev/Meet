package com.vladislaviliev.meet.session

import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryProvider
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.Koin

class SessionRepositoryTest {

    private val loginRepositoryProvider = mockk<LoginRepositoryProvider>(relaxed = true)
    private val client = mockk<OkHttpClient>(relaxed = true)
    private val sessionRepository = SessionRepository(Koin(), client, loginRepositoryProvider)

    @Test
    fun `initial state has no active session`() {
        assertNull(sessionRepository.currentScope)
        assertFalse(sessionRepository.isSessionActive)
    }

    @Test
    fun `startSession creates new scope and sets session as active`() {
        sessionRepository.startSession()
        assertNotNull(sessionRepository.currentScope)
        assertTrue(sessionRepository.isSessionActive)
    }

    @Test
    fun `startSession closes existing scope before creating new one`() {
        sessionRepository.startSession()
        val firstScope = sessionRepository.currentScope

        sessionRepository.startSession()
        val secondScope = sessionRepository.currentScope

        assertNotEquals(firstScope, secondScope)
        assertTrue(sessionRepository.isSessionActive)
    }

    @Test
    fun `endSession closes scope and sets session as inactive`() {
        sessionRepository.startSession()
        sessionRepository.endSession()

        assertNull(sessionRepository.currentScope)
        assertFalse(sessionRepository.isSessionActive)
    }

    @Test
    fun `endSession handles null scope gracefully`() {
        sessionRepository.endSession()

        assertNull(sessionRepository.currentScope)
        assertFalse(sessionRepository.isSessionActive)
    }

    @Test
    fun `multiple endSession calls are safe`() {
        sessionRepository.startSession()
        sessionRepository.endSession()
        sessionRepository.endSession()
        assertNull(sessionRepository.currentScope)
        assertFalse(sessionRepository.isSessionActive)
    }

    @Test
    fun `endSession clears login repository container`() {
        sessionRepository.startSession()
        sessionRepository.endSession()
        verify { loginRepositoryProvider.update(null) }
    }

    @Test
    fun `startSession clears login repository container when ending existing session`() {
        sessionRepository.startSession()
        sessionRepository.startSession()
        verify(exactly = 2) { loginRepositoryProvider.update(null) }
    }

    @Test
    fun `endSession cancels all network requests`() {
        sessionRepository.startSession()
        sessionRepository.endSession()
        verify { client.dispatcher.cancelAll() }
    }

    @Test
    fun `startSession cancels all network requests when ending existing session`() {
        sessionRepository.startSession()
        sessionRepository.startSession()
        verify(exactly = 2) { client.dispatcher.cancelAll() }
    }

    @Test
    fun `endSession cancels requests even with null scope`() {
        sessionRepository.endSession()
        verify { client.dispatcher.cancelAll() }
    }
}
