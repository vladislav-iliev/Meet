package com.vladislaviliev.meet.event

import org.koin.core.Koin
import org.koin.core.scope.Scope

class EventScopeRepository(private val koin: Koin) {

    var currentScope: Scope? = null
        private set

    var currentEventId: String? = null
        private set

    fun restartIfNeeded(eventId: String) {
        if (eventId == currentEventId) return

        currentScope?.close()
        currentScope = koin.createScope<EventScope>()

        currentEventId = eventId
    }
}