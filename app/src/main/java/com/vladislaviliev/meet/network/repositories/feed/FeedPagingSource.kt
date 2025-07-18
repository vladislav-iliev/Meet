package com.vladislaviliev.meet.network.repositories.feed

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vladislaviliev.meet.user.User
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.internal.http2.ConnectionShutdownException
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.models.PostResponseDto

internal class FeedPagingSource(
    private val dispatcher: CoroutineDispatcher,
    private val api: PostControllerApi,
    private val user: () -> User?,
) : PagingSource<Int, PostResponseDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PostResponseDto> {
        return try {
            val page = params.key ?: 0
            val user = user()

            user ?: return LoadResult.Error(ConnectionShutdownException())

            val response = withContext(dispatcher) {
                api.getAllPosts(
                    sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                    latitude = user.latitude,
                    longitude = user.longitude,
                    distance = 1_000_000,
                    pageNumber = page,
                    pageSize = params.loadSize
                )
            }

            val posts = response.data
            val isLastPage = response.lastPage

            LoadResult.Page(
                data = posts,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (isLastPage) null else page + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, PostResponseDto>) = state.anchorPosition?.let { anchorPosition ->
        val anchorPage = state.closestPageToPosition(anchorPosition)
        anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }
}
