package com.vladislaviliev.meet.network.repositories.event

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.PostControllerApi

internal class EventRepository(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val api: PostControllerApi,
    private val eventId: String,
) {
    private val _event = MutableStateFlow<Result<EventResponse>?>(null)
    val event = _event.asStateFlow()

    init {
        load()
    }

    private fun load() = scope.launch {
        _event.value = runCatching { download() }
    }

    private suspend fun download() = withContext(dispatcher) {
        EventResponse(
            api.getPostById(eventId),
            api.getPostParticipants(eventId, 0, 10).data.map { it.user.profilePhotos.first() }
        )
    }
}