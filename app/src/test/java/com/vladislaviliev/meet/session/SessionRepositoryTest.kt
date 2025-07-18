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
}
