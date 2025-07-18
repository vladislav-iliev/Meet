package com.vladislaviliev.meet.network.repositories.feed

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.testing.TestPager
import com.vladislaviliev.meet.network.repositories.user.User
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.models.BaseLocation
import org.openapitools.client.models.Interest
import org.openapitools.client.models.ListResponseDtoPostResponseDto
import org.openapitools.client.models.MiniUser
import org.openapitools.client.models.PostResponseDto
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FeedPagingSourceTest {

    private fun createMockApi() = mockk<PostControllerApi>()

    private val user = User(latitude = 40.7128, longitude = -74.0060)

    private fun createTestPostResponseDto(id: String, title: String) = PostResponseDto(
        id = id,
        title = title,
        images = listOf("image1.jpg"),
        location = mockk<BaseLocation>(),
        createdAt = OffsetDateTime.now(),
        interests = setOf(mockk<Interest>()),
        owner = mockk<MiniUser>(),
        payment = 0.0,
        currentUserStatus = PostResponseDto.CurrentUserStatus.NOT_PARTICIPATING,
        accessibility = PostResponseDto.Accessibility.PUBLIC,
        askToJoin = false,
        needsLocationalConfirmation = false,
        participantsCount = 5,
        status = PostResponseDto.Status.NOT_STARTED,
        savedByCurrentUser = false,
        blockedForCurrentUser = false,
        description = "Test description",
        maximumPeople = 10,
        toDate = OffsetDateTime.now().plusHours(2),
        fromDate = OffsetDateTime.now().plusHours(1),
        currency = null,
        currentUserRole = PostResponseDto.CurrentUserRole.NORMAL,
        currentUserArrivalStatus = PostResponseDto.CurrentUserArrivalStatus.NONE,
        rating = 4.5,
        clubId = null,
        chatRoomId = "chat-room-123"
    )

    @Test
    fun `load returns page when successful`() = runTest {
        val api = createMockApi()
        val testPosts = listOf(
            createTestPostResponseDto("1", "Post 1"),
            createTestPostResponseDto("2", "Post 2")
        )

        val mockResponse = ListResponseDtoPostResponseDto(
            data = testPosts,
            lastPage = false,
            listCount = testPosts.size.toLong()
        )

        coEvery {
            api.getAllPosts(
                sortBy = any(),
                latitude = any(),
                longitude = any(),
                distance = any(),
                pageNumber = any(),
                pageSize = any(),
                fromDate = any(),
                toDate = any()
            )
        } returns mockResponse

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(testPosts, result.data)
        assertNull(result.prevKey)
        assertEquals(1, result.nextKey)
    }

    @Test
    fun `load returns error when API throws exception`() = runTest {
        val api = createMockApi()
        val expectedException = RuntimeException("API Error")

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), any(), any(), any(), any())
        } throws expectedException

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals(expectedException::class, result.throwable::class)
        assertEquals(expectedException.message, result.throwable.message)
    }

    @Test
    fun `load returns empty page when no posts available`() = runTest {
        val api = createMockApi()
        val mockResponse = ListResponseDtoPostResponseDto(
            data = emptyList(),
            lastPage = true,
            listCount = 0
        )

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), any(), any(), any(), any())
        } returns mockResponse

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        assertTrue(result.data.isEmpty())
        assertNull(result.prevKey)
        assertNull(result.nextKey)
    }

    @Test
    fun `load handles last page correctly`() = runTest {
        val api = createMockApi()
        val testPosts = listOf(createTestPostResponseDto("1", "Post 1"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = testPosts,
            lastPage = true,
            listCount = testPosts.size.toLong()
        )

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), any(), any(), any(), any())
        } returns mockResponse

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(testPosts, result.data)
        assertNull(result.nextKey)
    }

    @Test
    fun `load handles append operation`() = runTest {
        val api = createMockApi()
        val testPosts = listOf(createTestPostResponseDto("3", "Post 3"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = testPosts,
            lastPage = false,
            listCount = testPosts.size.toLong()
        )

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), eq(2), any(), any(), any())
        } returns mockResponse

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 2,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(testPosts, result.data)
        assertEquals(1, result.prevKey)
        assertEquals(3, result.nextKey)
    }

    @Test
    fun `load handles prepend operation`() = runTest {
        val api = createMockApi()
        val testPosts = listOf(createTestPostResponseDto("0", "Post 0"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = testPosts,
            lastPage = false,
            listCount = testPosts.size.toLong()
        )

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), eq(0), any(), any(), any())
        } returns mockResponse

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Prepend(
                key = 0,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(testPosts, result.data)
        assertNull(result.prevKey)
        assertEquals(1, result.nextKey)
    }

    @Test
    fun `TestPager handles basic refresh operation`() = runTest {
        val api = createMockApi()
        val testPosts = listOf(createTestPostResponseDto("1", "Post 1"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = testPosts,
            lastPage = true,
            listCount = testPosts.size.toLong()
        )

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), any(), any(), any(), any())
        } returns mockResponse

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        val result = pager.refresh()
        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(testPosts, result.data)
    }

    @Test
    fun `TestPager handles append operations`() = runTest {
        val api = createMockApi()

        val firstPagePosts = listOf(createTestPostResponseDto("1", "Post 1"))
        val firstPageResponse = ListResponseDtoPostResponseDto(
            data = firstPagePosts,
            lastPage = false,
            listCount = firstPagePosts.size.toLong()
        )

        val secondPagePosts = listOf(createTestPostResponseDto("2", "Post 2"))
        val secondPageResponse = ListResponseDtoPostResponseDto(
            data = secondPagePosts,
            lastPage = true,
            listCount = secondPagePosts.size.toLong()
        )

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), eq(0), any(), any(), any())
        } returns firstPageResponse

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), eq(1), any(), any(), any())
        } returns secondPageResponse

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        val refreshResult = pager.refresh()
        assertTrue(refreshResult is PagingSource.LoadResult.Page)
        assertEquals(firstPagePosts, refreshResult.data)

        val appendResult = pager.append()
        assertTrue(appendResult is PagingSource.LoadResult.Page)
        assertEquals(secondPagePosts, appendResult.data)
    }

    @Test
    fun `TestPager handles error result`() = runTest {
        val api = createMockApi()
        val expectedException = RuntimeException("Network error")

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), any(), any(), any(), any())
        } throws expectedException

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        val result = pager.refresh()
        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals(expectedException::class, result.throwable::class)
        assertEquals(expectedException.message, result.throwable.message)
    }

    @Test
    fun `TestPager handles empty result`() = runTest {
        val api = createMockApi()
        val mockResponse = ListResponseDtoPostResponseDto(
            data = emptyList(),
            lastPage = true,
            listCount = 0
        )

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), any(), any(), any(), any())
        } returns mockResponse

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        val result = pager.refresh()
        assertTrue(result is PagingSource.LoadResult.Page)
        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    @Test
    fun `TestPager handles last page correctly`() = runTest {
        val api = createMockApi()
        val testPosts = listOf(createTestPostResponseDto("1", "Post 1"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = testPosts,
            lastPage = true,
            listCount = testPosts.size.toLong()
        )

        coEvery {
            api.getAllPosts(any(), any(), any(), any(), any(), any(), any(), any())
        } returns mockResponse

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        val result = pager.refresh()
        assertTrue(result is PagingSource.LoadResult.Page)
        assertEquals(testPosts, result.data)
        assertNull(result.nextKey)
    }

    @Test
    fun `getRefreshKey returns correct key`() = runTest {
        val api = createMockApi()
        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = api,
            user = user
        )

        val state = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(
                    data = listOf(createTestPostResponseDto("1", "Post 1")),
                    prevKey = null,
                    nextKey = 1
                )
            ),
            anchorPosition = 0,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0
        )

        val refreshKey = pagingSource.getRefreshKey(state)
        assertEquals(0, refreshKey)
    }
}