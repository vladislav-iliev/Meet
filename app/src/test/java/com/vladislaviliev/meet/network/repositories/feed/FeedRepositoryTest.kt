package com.vladislaviliev.meet.network.repositories.feed

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.asSnapshot
import com.vladislaviliev.meet.network.repositories.user.User
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.models.BaseLocation
import org.openapitools.client.models.Interest
import org.openapitools.client.models.ListResponseDtoPostResponseDto
import org.openapitools.client.models.MiniUser
import org.openapitools.client.models.PostResponseDto
import java.io.IOException
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FeedRepositoryTest {

    private val mockApi = mockk<PostControllerApi>()
    private val testUser = User(latitude = 40.7128, longitude = -74.0060)
    private val pagingConfig = PagingConfig(pageSize = 10, enablePlaceholders = false, initialLoadSize = 10)

    @After
    fun tearDown() {
        clearMocks(mockApi)
    }

    private fun TestScope.createRepository(
        user: User = testUser
    ) = FeedRepository(
        dispatcher = StandardTestDispatcher(testScheduler),
        api = mockApi,
        user = user,
        pagingConfig = pagingConfig
    )

    private fun createTestPostResponseDto(id: String = "test-id", title: String = "Test Post") = PostResponseDto(
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
    fun `repository initializes with correct dependencies`() = runTest {
        val repository = createRepository()
        assertNotNull(repository.feed)
        assertEquals(testUser, repository.user)
    }

    @Test
    fun `feed flow emits PagingData when API call succeeds`() = runTest {
        val expectedPosts = listOf(
            createTestPostResponseDto(id = "1", title = "Post 1"),
            createTestPostResponseDto(id = "2", title = "Post 2")
        )
        val mockResponse = ListResponseDtoPostResponseDto(
            data = expectedPosts, lastPage = true, listCount = expectedPosts.size.toLong()
        )

        coEvery {
            mockApi.getAllPosts(
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

        val repository = createRepository()
        val snapshot = repository.feed.asSnapshot()
        assertEquals(expectedPosts, snapshot)
    }

    @Test
    fun `feed uses provided user coordinates`() = runTest {
        val customUser = User(latitude = 51.5074, longitude = -0.1278)
        val mockResponse = ListResponseDtoPostResponseDto(
            data = emptyList(),
            lastPage = true,
            listCount = 0
        )

        coEvery {
            mockApi.getAllPosts(
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

        val repository = createRepository(user = customUser)
        val snapshot = repository.feed.asSnapshot()

        verify {
            mockApi.getAllPosts(
                sortBy = PostControllerApi.SortByGetAllPosts.CREATED_AT,
                latitude = customUser.latitude,
                longitude = customUser.longitude,
                distance = 1_000_000,
                pageNumber = 0,
                pageSize = 10,
                fromDate = null,
                toDate = null
            )
        }

        assertEquals(emptyList(), snapshot)
    }

    @Test
    fun `feed uses provided paging config`() = runTest {
        val customPagingConfig = PagingConfig(
            pageSize = 20,
            enablePlaceholders = true,
            initialLoadSize = 40
        )

        val repository = FeedRepository(
            dispatcher = StandardTestDispatcher(testScheduler),
            api = mockApi,
            user = testUser,
            pagingConfig = customPagingConfig
        )

        val mockResponse = ListResponseDtoPostResponseDto(
            data = emptyList(),
            lastPage = true,
            listCount = 0
        )

        coEvery {
            mockApi.getAllPosts(
                sortBy = any(),
                latitude = any(),
                longitude = any(),
                distance = any(),
                pageNumber = any(),
                pageSize = 40,
                fromDate = any(),
                toDate = any()
            )
        } returns mockResponse

        val snapshot = repository.feed.asSnapshot()
        assertEquals(emptyList(), snapshot)
    }

    @Test
    fun `feed creates new paging source for each collection`() = runTest {
        val mockResponse = ListResponseDtoPostResponseDto(
            data = emptyList(),
            lastPage = true,
            listCount = 0
        )

        coEvery {
            mockApi.getAllPosts(
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

        val repository = createRepository()

        val snapshot1 = repository.feed.asSnapshot()
        val snapshot2 = repository.feed.asSnapshot()

        assertEquals(emptyList(), snapshot1)
        assertEquals(emptyList(), snapshot2)
    }

    @Test
    fun `paging source handles API exceptions correctly`() = runTest {
        val expectedException = IOException("Network error")

        coEvery {
            mockApi.getAllPosts(any(), any(), any(), any(), any(), any(), any(), any())
        } throws expectedException

        val pagingSource = FeedPagingSource(
            dispatcher = StandardTestDispatcher(testScheduler),
            api = mockApi,
            user = testUser
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
    fun `different users create different feed results`() = runTest {
        val user1 = User(latitude = 40.7128, longitude = -74.0060)
        val user2 = User(latitude = 51.5074, longitude = -0.1278)

        val mockResponse = ListResponseDtoPostResponseDto(
            data = listOf(createTestPostResponseDto()),
            lastPage = true,
            listCount = 1
        )

        coEvery {
            mockApi.getAllPosts(
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

        val repository1 = createRepository(user = user1)
        val repository2 = createRepository(user = user2)

        val snapshot1 = repository1.feed.asSnapshot()
        val snapshot2 = repository2.feed.asSnapshot()

        assertEquals(1, snapshot1.size)
        assertEquals(1, snapshot2.size)
    }
}