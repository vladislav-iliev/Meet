package com.vladislaviliev.meet.network.repositories.feed

import com.vladislaviliev.meet.network.repositories.user.User
import kotlinx.coroutines.CoroutineDispatcher
import org.openapitools.client.apis.PostControllerApi

internal class FeedRepository(
    private val dispatcher: CoroutineDispatcher, private val api: PostControllerApi, val user: User
) {
    val pagingSource get() = FeedPagingSource(dispatcher, api, user)
}