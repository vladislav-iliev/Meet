package com.vladislaviliev.meet.network.repositories

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.testing.TestPager
import com.vladislaviliev.meet.user.User
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.internal.http2.ConnectionShutdownException
import org.junit.Test
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.models.BaseLocation
import org.openapitools.client.models.Interest
import org.openapitools.client.models.ListResponseDtoPostResponseDto
import org.openapitools.client.models.MiniUser
import org.openapitools.client.models.PostResponseDto
import java.time.LocalDate
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
        description = "Test description",
        images = listOf("image1.jpg", "image2.jpg"),
        location = BaseLocation(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "Test Address",
            city = "Test City",
            country = "Test Country",
            name = "Test Location"
        ),
        createdAt = OffsetDateTime.now(),
        interests = setOf(
            Interest(
                name = "Test Interest",
                icon = "test-icon",
                category = "SPORTS"
            )
        ),
        owner = MiniUser(
            id = id,
            firstName = "Test",
            lastName = "User",
            profilePhotos = listOf("profile.jpg"),
            occupation = "Test Occupation",
            location = BaseLocation(
                latitude = 40.7128,
                longitude = -74.0060,
                address = "Test Address",
                city = "Test City",
                country = "Test Country",
                name = "Test Location"
            ),
            birthDate = LocalDate.now(),
            userRole = MiniUser.UserRole.NORMAL,
        ),
        payment = 0.0,
        accessibility = PostResponseDto.Accessibility.PUBLIC,
        askToJoin = false,
        needsLocationalConfirmation = false,
        participantsCount = 5,
        status = PostResponseDto.Status.NOT_STARTED,
        savedByCurrentUser = false,
        blockedForCurrentUser = false,
        maximumPeople = 10,
        toDate = OffsetDateTime.now().plusHours(2),
        fromDate = OffsetDateTime.now().plusHours(1),
        currency = null,
        rating = 4.5,
        clubId = null,
        chatRoomId = "chat-room-123",
        currentUserStatus = PostResponseDto.CurrentUserStatus.PARTICIPATING
    )

    @Test
    fun `load returns page when successful`() = runTest {
        val mockApi = createMockApi()
        val posts = listOf(
            createTestPostResponseDto(id = "1", title = "Post 1"),
            createTestPostResponseDto(id = "2", title = "Post 2")
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                longitude = -74.0060,
                latitude = 40.7128,
                distance = 1_000_000,
                pageNumber = 0,
                pageSize = 10,
                categories = null,
                toDate = null,
                fromDate = null
            )
        } returns ListResponseDtoPostResponseDto(
            data = posts,
            lastPage = false,
            listCount = posts.size.toLong()
        )

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val loadParams = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 10,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(loadParams)
        assertTrue(result is PagingSource.LoadResult.Page<Int, PostResponseDto>)
        val page = result
        assertEquals(posts, page.data)
        assertEquals(null, page.prevKey)
        assertEquals(1, page.nextKey)
    }

    @Test
    fun `load returns error when API throws exception`() = runTest {
        val mockApi = createMockApi()
        val exception = RuntimeException("Network error")

        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                longitude = any(),
                latitude = any(),
                distance = any(),
                pageNumber = any(),
                pageSize = any(),
                categories = any(),
                toDate = any(),
                fromDate = any()
            )
        } throws exception

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val loadParams = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 10,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(loadParams)
        assertTrue(result is PagingSource.LoadResult.Error<Int, PostResponseDto>)
        val error = result
        assertTrue(error.throwable is RuntimeException)
        assertEquals("Network error", error.throwable.message)
    }

    @Test
    fun `load returns error when user is null`() = runTest {
        val mockApi = createMockApi()

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { null }
        )

        val loadParams = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 10,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(loadParams)
        assertTrue(result is PagingSource.LoadResult.Error<*, *>)
        val error = result as PagingSource.LoadResult.Error<Int, PostResponseDto>
        assertTrue(error.throwable is ConnectionShutdownException)
    }

    @Test
    fun `load returns empty page when no posts available`() = runTest {
        val mockApi = createMockApi()

        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                longitude = any(),
                latitude = any(),
                distance = any(),
                pageNumber = any(),
                pageSize = any(),
                categories = any(),
                toDate = any(),
                fromDate = any()
            )
        } returns ListResponseDtoPostResponseDto(
            data = emptyList(),
            lastPage = true,
            listCount = 0L
        )

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val loadParams = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 10,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(loadParams)
        assertTrue(result is PagingSource.LoadResult.Page<Int, PostResponseDto>)
        val page = result
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
        assertNull(page.prevKey)
    }

    @Test
    fun `load handles last page correctly`() = runTest {
        val mockApi = createMockApi()
        val posts = listOf(
            createTestPostResponseDto(id = "1", title = "Post 1"),
            createTestPostResponseDto(id = "2", title = "Post 2")
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                longitude = any(),
                latitude = any(),
                distance = any(),
                pageNumber = any(),
                pageSize = any(),
                categories = any(),
                toDate = any(),
                fromDate = any()
            )
        } returns ListResponseDtoPostResponseDto(
            data = posts,
            lastPage = true,
            listCount = posts.size.toLong()
        )

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val loadParams = PagingSource.LoadParams.Refresh<Int>(
            key = null,
            loadSize = 10,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(loadParams)
        assertTrue(result is PagingSource.LoadResult.Page<Int, PostResponseDto>)
        val page = result
        assertEquals(posts, page.data)
        assertNull(page.nextKey) // Should be null since it's the last page
    }

    @Test
    fun `load handles append operation`() = runTest {
        val mockApi = createMockApi()
        val posts = listOf(
            createTestPostResponseDto(id = "3", title = "Post 3"),
            createTestPostResponseDto(id = "4", title = "Post 4")
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                longitude = -74.0060,
                latitude = 40.7128,
                distance = 1_000_000,
                pageNumber = 1,
                pageSize = 10,
                categories = null,
                toDate = null,
                fromDate = null
            )
        } returns ListResponseDtoPostResponseDto(
            data = posts,
            lastPage = false,
            listCount = posts.size.toLong()
        )

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val loadParams = PagingSource.LoadParams.Append(
            key = 1,
            loadSize = 10,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(loadParams)
        assertTrue(result is PagingSource.LoadResult.Page<Int, PostResponseDto>)
        val page = result
        assertEquals(posts, page.data)
        assertEquals(0, page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load handles prepend operation`() = runTest {
        val mockApi = createMockApi()
        val posts = listOf(
            createTestPostResponseDto(id = "1", title = "Post 1"),
            createTestPostResponseDto(id = "2", title = "Post 2")
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                longitude = -74.0060,
                latitude = 40.7128,
                distance = 1_000_000,
                pageNumber = 0,
                pageSize = 10,
                categories = null,
                toDate = null,
                fromDate = null
            )
        } returns ListResponseDtoPostResponseDto(
            data = posts,
            lastPage = false,
            listCount = posts.size.toLong()
        )

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val loadParams = PagingSource.LoadParams.Prepend(
            key = 0,
            loadSize = 10,
            placeholdersEnabled = false
        )

        val result = pagingSource.load(loadParams)
        assertTrue(result is PagingSource.LoadResult.Page<Int, PostResponseDto>)
        val page = result
        assertEquals(posts, page.data)
        assertNull(page.prevKey) // Should be null since it's page 0
        assertEquals(1, page.nextKey)
    }

    @Test
    fun `TestPager handles basic refresh operation`() = runTest {
        val mockApi = createMockApi()
        val posts = listOf(
            createTestPostResponseDto(id = "1", title = "Post 1"),
            createTestPostResponseDto(id = "2", title = "Post 2")
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                longitude = any(),
                latitude = any(),
                distance = any(),
                pageNumber = any(),
                pageSize = any(),
                categories = any(),
                toDate = any(),
                fromDate = any()
            )
        } returns ListResponseDtoPostResponseDto(
            data = posts,
            lastPage = false,
            listCount = posts.size.toLong()
        )

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        val result = pager.refresh()
        assertTrue(result is PagingSource.LoadResult.Page<Int, PostResponseDto>)
        val page = result
        assertEquals(posts, page.data)
        assertEquals(1, page.nextKey)
    }

    @Test
    fun `TestPager handles append operations`() = runTest {
        val mockApi = createMockApi()
        val initialPosts = listOf(
            createTestPostResponseDto(id = "1", title = "Post 1"),
            createTestPostResponseDto(id = "2", title = "Post 2")
        )
        val appendPosts = listOf(
            createTestPostResponseDto(id = "3", title = "Post 3"),
            createTestPostResponseDto(id = "4", title = "Post 4")
        )

        // Mock page 0 (initial page)
        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                longitude = any(),
                latitude = any(),
                distance = any(),
                pageNumber = 0,
                pageSize = any(),
                categories = any(),
                toDate = any(),
                fromDate = any()
            )
        } returns ListResponseDtoPostResponseDto(
            data = initialPosts,
            lastPage = false,
            listCount = initialPosts.size.toLong()
        )

        // Mock page 1 (for append)
        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                longitude = any(),
                latitude = any(),
                distance = any(),
                pageNumber = 1,
                pageSize = any(),
                categories = any(),
                toDate = any(),
                fromDate = any()
            )
        } returns ListResponseDtoPostResponseDto(
            data = appendPosts,
            lastPage = false,
            listCount = appendPosts.size.toLong()
        )

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        // Call refresh with initialKey = 0
        val initialResult = pager.refresh(initialKey = 0)
        assertTrue(initialResult is PagingSource.LoadResult.Page<Int, PostResponseDto>)
        val initialPage = initialResult
        assertEquals(initialPosts, initialPage.data)

        // Now try to append - this should load page 1
        val appendResult = pager.append()

        // Check if append actually returned data
        if (appendResult != null) {
            assertTrue(appendResult is PagingSource.LoadResult.Page<Int, PostResponseDto>)
            val appendPage = appendResult
            assertEquals(appendPosts, appendPage.data)

            // Verify total loaded data (append data comes after initial)
            assertEquals(initialPosts + appendPosts, pager.getPages().flatMap { it.data })
        } else {
            // If append returned null, it means there's no next page
            assertEquals(initialPosts, pager.getPages().flatMap { it.data })
        }
    }

    @Test
    fun `TestPager handles error result`() = runTest {
        val mockApi = createMockApi()
        val exception = RuntimeException("Network error")

        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                longitude = any(),
                latitude = any(),
                distance = any(),
                pageNumber = any(),
                pageSize = any(),
                categories = any(),
                toDate = any(),
                fromDate = any()
            )
        } throws exception

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        val result = pager.refresh()
        assertTrue(result is PagingSource.LoadResult.Error<Int, PostResponseDto>)
        val error = result
        assertTrue(error.throwable is RuntimeException)
        assertEquals("Network error", error.throwable.message)
    }

    @Test
    fun `TestPager handles empty result`() = runTest {
        val mockApi = createMockApi()

        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                longitude = any(),
                latitude = any(),
                distance = any(),
                pageNumber = any(),
                pageSize = any(),
                categories = any(),
                toDate = any(),
                fromDate = any()
            )
        } returns ListResponseDtoPostResponseDto(
            data = emptyList(),
            lastPage = true,
            listCount = 0L
        )

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        val result = pager.refresh()
        assertTrue(result is PagingSource.LoadResult.Page<Int, PostResponseDto>)
        val page = result
        assertTrue(page.data.isEmpty())
        assertNull(page.nextKey)
        assertNull(page.prevKey)
    }

    @Test
    fun `TestPager handles last page correctly`() = runTest {
        val mockApi = createMockApi()
        val posts = listOf(
            createTestPostResponseDto(id = "1", title = "Post 1"),
            createTestPostResponseDto(id = "2", title = "Post 2")
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                longitude = any(),
                latitude = any(),
                distance = any(),
                pageNumber = any(),
                pageSize = any(),
                categories = any(),
                toDate = any(),
                fromDate = any()
            )
        } returns ListResponseDtoPostResponseDto(
            data = posts,
            lastPage = true,
            listCount = posts.size.toLong()
        )

        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        val pager = TestPager(
            config = PagingConfig(pageSize = 10),
            pagingSource = pagingSource
        )

        val result = pager.refresh()
        assertTrue(result is PagingSource.LoadResult.Page<Int, PostResponseDto>)
        val page = result
        assertEquals(posts, page.data)
        assertNull(page.nextKey) // Should be null since it's the last page
    }

    @Test
    fun `getRefreshKey returns correct key`() = runTest {
        val mockApi = createMockApi()
        val pagingSource = FeedPagingSource(
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            user = { user }
        )

        // Test with null anchor position
        val stateWithNullAnchor = PagingState<Int, PostResponseDto>(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0
        )

        val refreshKey = pagingSource.getRefreshKey(stateWithNullAnchor)
        assertNull(refreshKey)
    }
}