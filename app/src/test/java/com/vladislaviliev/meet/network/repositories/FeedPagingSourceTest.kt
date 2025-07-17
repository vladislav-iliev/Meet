package com.vladislaviliev.meet.network.repositories

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vladislaviliev.meet.user.User
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.internal.http2.ConnectionShutdownException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.models.BaseLocation
import org.openapitools.client.models.Interest
import org.openapitools.client.models.ListResponseDtoPostResponseDto
import org.openapitools.client.models.MiniUser
import org.openapitools.client.models.PostResponseDto
import java.io.IOException
import java.time.OffsetDateTime
import kotlin.test.assertNull

class FeedPagingSourceTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockApi = mockk<PostControllerApi>()
    private val testUser = User(latitude = 40.7128, longitude = -74.0060)

    private fun createPagingSource(userProvider: () -> User? = { testUser }) = FeedPagingSource(
        dispatcher = testDispatcher,
        api = mockApi,
        user = userProvider
    )

    private fun createTestPostResponseDto(
        id: String = "test-id",
        title: String = "Test Post"
    ) = PostResponseDto(
        id = id,
        title = title,
        images = listOf("image1.jpg", "image2.jpg"),
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
    fun `load returns Page when user is available and API call succeeds`() = runTest(testDispatcher) {
        val expectedPosts = listOf(
            createTestPostResponseDto(id = "1", title = "Post 1"),
            createTestPostResponseDto(id = "2", title = "Post 2")
        )
        val mockResponse = ListResponseDtoPostResponseDto(
            data = expectedPosts,
            lastPage = false,
            listCount = expectedPosts.size.toLong()
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                latitude = testUser.latitude,
                longitude = testUser.longitude,
                distance = 1_000_000,
                pageNumber = 0,
                pageSize = 10
            )
        } returns mockResponse

        val pagingSource = createPagingSource()

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(expectedPosts, page.data)
        assertNull(page.prevKey)
        assertEquals(1, page.nextKey)
    }

    @Test
    fun `load returns Page with null nextKey when last page is reached`() = runTest(testDispatcher) {
        val expectedPosts = listOf(createTestPostResponseDto(id = "1", title = "Last post"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = expectedPosts,
            lastPage = true,
            listCount = expectedPosts.size.toLong()
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                latitude = testUser.latitude,
                longitude = testUser.longitude,
                distance = 1_000_000,
                pageNumber = 2,
                pageSize = 10
            )
        } returns mockResponse

        val pagingSource = createPagingSource()

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 2,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(expectedPosts, page.data)
        assertEquals(1, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `load returns Error when user is null`() = runTest(testDispatcher) {
        val pagingSource = createPagingSource(userProvider = { null })

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertTrue(error.throwable is ConnectionShutdownException)
    }

    @Test
    fun `load returns Error when API call fails`() = runTest(testDispatcher) {
        val expectedException = IOException("Network error")

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                latitude = testUser.latitude,
                longitude = testUser.longitude,
                distance = 1_000_000,
                pageNumber = 0,
                pageSize = 10
            )
        } throws expectedException

        val pagingSource = createPagingSource()

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        val error = result as PagingSource.LoadResult.Error
        assertSame(expectedException.javaClass, error.throwable.javaClass)
        assertEquals(expectedException.message, error.throwable.message)
    }

    @Test
    fun `load handles different page keys correctly`() = runTest(testDispatcher) {
        val expectedPosts = listOf(createTestPostResponseDto(id = "3", title = "Page 3"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = expectedPosts,
            lastPage = false,
            listCount = expectedPosts.size.toLong()
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                latitude = testUser.latitude,
                longitude = testUser.longitude,
                distance = 1_000_000,
                pageNumber = 3,
                pageSize = 5
            )
        } returns mockResponse

        val pagingSource = createPagingSource()

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 3,
                loadSize = 5,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(expectedPosts, page.data)
        assertEquals(2, page.prevKey)
        assertEquals(4, page.nextKey)
    }

    @Test
    fun `load handles empty response correctly`() = runTest(testDispatcher) {
        val mockResponse = ListResponseDtoPostResponseDto(
            data = emptyList(),
            lastPage = true,
            listCount = 0
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                latitude = testUser.latitude,
                longitude = testUser.longitude,
                distance = 1_000_000,
                pageNumber = 0,
                pageSize = 10
            )
        } returns mockResponse

        val pagingSource = createPagingSource()

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(emptyList<PostResponseDto>(), page.data)
        assertNull(page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `getRefreshKey returns correct key`() {
        val pagingSource = createPagingSource()
        val mockState = mockk<PagingState<Int, PostResponseDto>>()
        val mockPage = mockk<PagingSource.LoadResult.Page<Int, PostResponseDto>>()

        every { mockState.anchorPosition } returns 15
        every { mockState.closestPageToPosition(15) } returns mockPage
        every { mockPage.prevKey } returns 1
        every { mockPage.nextKey } returns 3

        assertEquals(2, pagingSource.getRefreshKey(mockState))
    }

    @Test
    fun `getRefreshKey returns null when anchorPosition is null`() {
        val pagingSource = createPagingSource()
        val mockState = mockk<PagingState<Int, PostResponseDto>>()
        every { mockState.anchorPosition } returns null
        assertNull(pagingSource.getRefreshKey(mockState))
    }

    @Test
    fun `load first page has null prevKey`() = runTest(testDispatcher) {
        val expectedPosts = listOf(createTestPostResponseDto(id = "1", title = "First page post"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = expectedPosts,
            lastPage = false,
            listCount = expectedPosts.size.toLong()
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                latitude = testUser.latitude,
                longitude = testUser.longitude,
                distance = 1_000_000,
                pageNumber = 0,
                pageSize = 10
            )
        } returns mockResponse

        val pagingSource = createPagingSource()

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertNull(page.prevKey)
        assertEquals(1, page.nextKey)
    }

    @Test
    fun `load with Prepend params works correctly`() = runTest(testDispatcher) {
        val expectedPosts = listOf(createTestPostResponseDto(id = "prepend", title = "Prepend post"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = expectedPosts,
            lastPage = false,
            listCount = expectedPosts.size.toLong()
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                latitude = testUser.latitude,
                longitude = testUser.longitude,
                distance = 1_000_000,
                pageNumber = 1,
                pageSize = 10
            )
        } returns mockResponse

        val pagingSource = createPagingSource()

        val result = pagingSource.load(
            PagingSource.LoadParams.Prepend(
                key = 1,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertEquals(expectedPosts, page.data)
        assertEquals(0, page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `load with page key 0 has null prevKey`() = runTest(testDispatcher) {
        val expectedPosts = listOf(createTestPostResponseDto(id = "0", title = "Page 0 post"))
        val mockResponse = ListResponseDtoPostResponseDto(
            data = expectedPosts,
            lastPage = false,
            listCount = expectedPosts.size.toLong()
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                latitude = testUser.latitude,
                longitude = testUser.longitude,
                distance = 1_000_000,
                pageNumber = 0,
                pageSize = 10
            )
        } returns mockResponse

        val pagingSource = createPagingSource()

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Page)
        val page = result as PagingSource.LoadResult.Page
        assertNull(page.prevKey)
        assertEquals(1, page.nextKey)
    }

    @Test
    fun `getRefreshKey returns null when closestPageToPosition returns null`() {
        val pagingSource = createPagingSource()
        val mockState = mockk<PagingState<Int, PostResponseDto>>()

        every { mockState.anchorPosition } returns 15
        every { mockState.closestPageToPosition(15) } returns null

        assertNull(pagingSource.getRefreshKey(mockState))
    }

    @Test
    fun `getRefreshKey handles page with null prevKey`() {
        val pagingSource = createPagingSource()
        val mockState = mockk<PagingState<Int, PostResponseDto>>()
        val mockPage = mockk<PagingSource.LoadResult.Page<Int, PostResponseDto>>()

        every { mockState.anchorPosition } returns 15
        every { mockState.closestPageToPosition(15) } returns mockPage
        every { mockPage.prevKey } returns null
        every { mockPage.nextKey } returns 2

        assertEquals(1, pagingSource.getRefreshKey(mockState))
    }

    @Test
    fun `getRefreshKey handles page with null nextKey`() {
        val pagingSource = createPagingSource()
        val mockState = mockk<PagingState<Int, PostResponseDto>>()
        val mockPage = mockk<PagingSource.LoadResult.Page<Int, PostResponseDto>>()

        every { mockState.anchorPosition } returns 15
        every { mockState.closestPageToPosition(15) } returns mockPage
        every { mockPage.prevKey } returns 3
        every { mockPage.nextKey } returns null

        assertEquals(4, pagingSource.getRefreshKey(mockState))
    }
}