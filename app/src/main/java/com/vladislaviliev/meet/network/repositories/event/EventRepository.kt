package com.vladislaviliev.meet.network.repositories.event

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.PostControllerApi

internal class EventRepository(
    private val dispatcher: CoroutineDispatcher,
    private val api: PostControllerApi,
    private val eventId: String,
) {
    private val _event = MutableStateFlow<Event>(Event.Blank)
    val event = _event.asStateFlow()

    private suspend fun downloadResponse() = withContext(dispatcher) {
        runCatching {
            val post = api.getPostById(eventId)
            val participants =
                api.getPostParticipants(eventId, 0, 10).data.map { it.user.profilePhotos.first() }
            Event.Success(post, participants)
        }
    }

    suspend fun download(): Result<Unit> {
        val response = downloadResponse()
        if (response.isSuccess) {
            _event.value = response.getOrNull()!!
            return Result.success(Unit)
        }
        _event.value = Event.Blank
        return Result.failure(response.exceptionOrNull()!!)
    }
}