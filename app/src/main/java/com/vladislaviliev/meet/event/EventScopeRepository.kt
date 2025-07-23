package com.vladislaviliev.meet.event

import org.koin.core.Koin
import org.koin.core.scope.Scope

class EventScopeRepository(private val koin: Koin) {

    var currentScope: Scope? = null
        private set

    private var currentEventId: String? = null

    fun restartIfNeeded(eventId: String) {
        if (eventId == currentEventId) return

        currentScope?.close()
        currentScope = koin.createScope<EventScope>()

        currentEventId = eventId
    }
}