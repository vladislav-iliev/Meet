package com.vladislaviliev.meet.event

import org.junit.Test
import org.koin.core.Koin
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class EventScopeRepositoryTest {

    private val koin = Koin()
    private val eventScopeRepository = EventScopeRepository(koin)

    @Test
    fun `restartIfNeeded creates a new scope if eventId is different`() {
        eventScopeRepository.restartIfNeeded("1")
        assertNotNull(eventScopeRepository.currentScope)

        val firstCreated = eventScopeRepository.currentScope

        eventScopeRepository.restartIfNeeded("2")
        assertNotSame(firstCreated, eventScopeRepository.currentScope)
    }


    @Test
    fun `restartIfNeeded does nothing if eventId is the same`() {
        val eventId = "event123"

        eventScopeRepository.restartIfNeeded(eventId)
        val firstScope = eventScopeRepository.currentScope

        eventScopeRepository.restartIfNeeded(eventId)
        assertSame(firstScope, eventScopeRepository.currentScope)
    }

    @Test
    fun `restartIfNeeded closes the old scope when a new one is created`() {
        eventScopeRepository.restartIfNeeded("event123")
        val firstScope = eventScopeRepository.currentScope!!

        eventScopeRepository.restartIfNeeded("event456")

        assertTrue(firstScope.closed)
    }
}