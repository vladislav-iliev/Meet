package com.vladislaviliev.meet.network.repositories.event

import org.openapitools.client.models.PostResponseDto

internal sealed class Event {
    object Blank : Event()
    data class Success(val postResponseDto: PostResponseDto, val participants: Iterable<String>) : Event()
}
