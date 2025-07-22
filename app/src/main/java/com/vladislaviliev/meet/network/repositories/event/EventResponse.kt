package com.vladislaviliev.meet.network.repositories.event

import org.openapitools.client.models.PostResponseDto

internal data class EventResponse(val postResponseDto: PostResponseDto, val participantsProfilePics: Iterable<String>)
