package com.vladislaviliev.meet.network.repositories.feed

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.vladislaviliev.meet.network.repositories.user.UserState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.StateFlow
import org.openapitools.client.apis.PostControllerApi

internal class FeedRepository(
    private val dispatcher: CoroutineDispatcher,
    private val api: PostControllerApi,
    val userState: StateFlow<UserState>,
    pagingConfig: PagingConfig,
) {
    val feed = Pager(
        pagingConfig,
        pagingSourceFactory = { FeedPagingSource(dispatcher, api, { userState.value.getOrNull() }) }
    ).flow
}