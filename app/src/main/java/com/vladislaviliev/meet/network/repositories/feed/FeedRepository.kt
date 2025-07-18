package com.vladislaviliev.meet.network.repositories.feed

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.vladislaviliev.meet.network.repositories.user.User
import kotlinx.coroutines.CoroutineDispatcher
import org.openapitools.client.apis.PostControllerApi

internal class FeedRepository(
    private val dispatcher: CoroutineDispatcher,
    private val api: PostControllerApi,
    val user: User,
    pagingConfig: PagingConfig,
) {
    val feed = Pager(
        pagingConfig,
        pagingSourceFactory = { FeedPagingSource(dispatcher, api, user) }
    ).flow
}