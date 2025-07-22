package com.vladislaviliev.meet.session

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.core.Koin

class SessionRepositoryTest {

    private val sessionRepository = SessionRepository(Koin())

    @Test
    fun `initial state has no active session`() {
        assertNull(sessionRepository.currentScope)
        assertFalse(sessionRepository.isSessionActive.value)
    }

    @Test
    fun `restartSession creates new scope and sets session as active`() {
        sessionRepository.restartSession()
        assertNotNull(sessionRepository.currentScope)
        assertTrue(sessionRepository.isSessionActive.value)
    }

    @Test
    fun `restartSession closes existing scope before creating new one`() {
        sessionRepository.restartSession()
        val firstScope = sessionRepository.currentScope

        sessionRepository.restartSession()
        val secondScope = sessionRepository.currentScope

        assertNotEquals(firstScope, secondScope)
        assertTrue(sessionRepository.isSessionActive.value)
    }

    @Test
    fun `endSession closes scope and sets session as inactive`() {
        sessionRepository.restartSession()
        sessionRepository.endSession()

        assertNull(sessionRepository.currentScope)
        assertFalse(sessionRepository.isSessionActive.value)
    }

    @Test
    fun `endSession handles null scope gracefully`() {
        sessionRepository.endSession()

        assertNull(sessionRepository.currentScope)
        assertFalse(sessionRepository.isSessionActive.value)
    }

    @Test
    fun `multiple endSession calls are safe`() {
        sessionRepository.restartSession()
        sessionRepository.endSession()
        sessionRepository.endSession()
        assertNull(sessionRepository.currentScope)
        assertFalse(sessionRepository.isSessionActive.value)
    }
}
