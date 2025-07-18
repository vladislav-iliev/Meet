package com.vladislaviliev.meet.network.repositories.event

import com.vladislaviliev.meet.network.Tokens
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.models.PostResponseDto

internal class EventRepository(
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val api: PostControllerApi,
    private val eventId: String,
    val loginTokens: StateFlow<Tokens>
) {
    private val _event = MutableStateFlow<PostResponseDto?>(null)
    val event = _event.asStateFlow()

    init {
        load()
    }

    private fun load() = scope.launch {
        val apiResponse = runCatching { withContext(dispatcher) { api.getPostById(eventId) } }
        _event.value = apiResponse.getOrNull()
    }
}