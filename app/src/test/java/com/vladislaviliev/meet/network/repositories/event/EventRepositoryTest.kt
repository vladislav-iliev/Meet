package com.vladislaviliev.meet.network.repositories.event

import com.vladislaviliev.meet.network.Tokens
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.infrastructure.ServerException
import org.openapitools.client.models.BaseLocation
import org.openapitools.client.models.Interest
import org.openapitools.client.models.MiniUser
import org.openapitools.client.models.PostResponseDto
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class EventRepositoryTest {

    private val eventId = "test-event-id"
    private val specificEventId = "specific-event-123"
    private val standardTokens = Tokens("user123", "access-token", "refresh-token", 123456789L)
    private val mockLocation = mockk<BaseLocation>()
    private val mockOwner = mockk<MiniUser>()
    private val mockInterests = setOf<Interest>()

    private fun createMockEvent(
        id: String = eventId,
        title: String = "Test Event",
        images: List<String> = emptyList(),
        payment: Double = 0.0,
        status: PostResponseDto.Status = PostResponseDto.Status.NOT_STARTED,
        accessibility: PostResponseDto.Accessibility = PostResponseDto.Accessibility.PUBLIC,
        currentUserStatus: PostResponseDto.CurrentUserStatus = PostResponseDto.CurrentUserStatus.NOT_PARTICIPATING,
        description: String? = null,
        askToJoin: Boolean = false,
        needsLocationalConfirmation: Boolean = false,
        participantsCount: Int = 0,
        savedByCurrentUser: Boolean = false,
        blockedForCurrentUser: Boolean = false
    ) = PostResponseDto(
        id = id,
        title = title,
        images = images,
        location = mockLocation,
        createdAt = OffsetDateTime.now(),
        interests = mockInterests,
        owner = mockOwner,
        payment = payment,
        currentUserStatus = currentUserStatus,
        accessibility = accessibility,
        askToJoin = askToJoin,
        needsLocationalConfirmation = needsLocationalConfirmation,
        participantsCount = participantsCount,
        status = status,
        savedByCurrentUser = savedByCurrentUser,
        blockedForCurrentUser = blockedForCurrentUser,
        description = description
    )

    @Test
    fun `should load event successfully on initialization`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val loginTokens = MutableStateFlow(standardTokens)

        val expectedEvent = createMockEvent(
            title = "Test Event",
            images = listOf("image1.jpg", "image2.jpg"),
            payment = 25.0,
            needsLocationalConfirmation = true,
            participantsCount = 5,
            description = "Test Event Description"
        )

        coEvery { mockApi.getPostById(eventId) } returns expectedEvent

        val repository = EventRepository(
            scope = testScope,
            dispatcher = testDispatcher,
            api = mockApi,
            eventId = eventId,
            loginTokens = loginTokens
        )

        testScheduler.advanceUntilIdle()
        coVerify { mockApi.getPostById(eventId) }
        assertEquals(expectedEvent, repository.event.first())
    }

    @Test
    fun `should handle ClientException gracefully`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val loginTokens = MutableStateFlow(standardTokens)

        coEvery { mockApi.getPostById(eventId) } throws ClientException("Client error : 404 Not Found", 404, mockk())

        val repository = EventRepository(
            scope = testScope,
            dispatcher = testDispatcher,
            api = mockApi,
            eventId = eventId,
            loginTokens = loginTokens
        )

        testScheduler.advanceUntilIdle()
        coVerify { mockApi.getPostById(eventId) }
        assertNull(repository.event.first())
    }

    @Test
    fun `should handle ServerException gracefully`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val loginTokens = MutableStateFlow(standardTokens)

        coEvery { mockApi.getPostById(eventId) } throws ServerException(
            "Server error : 500 Internal Server Error",
            500,
            mockk()
        )

        val repository = EventRepository(
            scope = testScope,
            dispatcher = testDispatcher,
            api = mockApi,
            eventId = eventId,
            loginTokens = loginTokens
        )

        testScheduler.advanceUntilIdle()
        coVerify { mockApi.getPostById(eventId) }
        assertNull(repository.event.first())
    }

    @Test
    fun `should handle IllegalStateException gracefully`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val loginTokens = MutableStateFlow(standardTokens)

        coEvery { mockApi.getPostById(eventId) } throws IllegalStateException("Request not correctly configured")

        val repository = EventRepository(
            scope = testScope,
            dispatcher = testDispatcher,
            api = mockApi,
            eventId = eventId,
            loginTokens = loginTokens
        )

        testScheduler.advanceUntilIdle()
        coVerify { mockApi.getPostById(eventId) }
        assertNull(repository.event.first())
    }

    @Test
    fun `should call API with correct event ID`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val loginTokens = MutableStateFlow(standardTokens)

        val mockEvent = createMockEvent(
            id = specificEventId,
            title = "Mock Event"
        )

        coEvery { mockApi.getPostById(any()) } returns mockEvent

        EventRepository(
            scope = testScope,
            dispatcher = testDispatcher,
            api = mockApi,
            eventId = specificEventId,
            loginTokens = loginTokens
        )

        testScheduler.advanceUntilIdle()
        coVerify { mockApi.getPostById(specificEventId) }
    }

    @Test
    fun `should initialize with null event state`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val loginTokens = MutableStateFlow(standardTokens)

        val mockEvent = createMockEvent()

        coEvery { mockApi.getPostById(eventId) } returns mockEvent

        val repository = EventRepository(
            scope = testScope,
            dispatcher = testDispatcher,
            api = mockApi,
            eventId = eventId,
            loginTokens = loginTokens
        )

        assertNull(repository.event.value)
    }

    @Test
    fun `should update event state after successful API call`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val loginTokens = MutableStateFlow(standardTokens)

        val expectedEvent = createMockEvent(
            title = "Updated Event",
            images = listOf("updated.jpg"),
            payment = 50.0,
            currentUserStatus = PostResponseDto.CurrentUserStatus.PARTICIPATING,
            accessibility = PostResponseDto.Accessibility.PRIVATE,
            askToJoin = true,
            needsLocationalConfirmation = true,
            participantsCount = 10,
            status = PostResponseDto.Status.HAS_STARTED,
            savedByCurrentUser = true,
            description = "Updated Content"
        )

        coEvery { mockApi.getPostById(eventId) } returns expectedEvent

        val repository = EventRepository(
            scope = testScope,
            dispatcher = testDispatcher,
            api = mockApi,
            eventId = eventId,
            loginTokens = loginTokens
        )

        assertNull(repository.event.value)
        testScheduler.advanceUntilIdle()
        assertEquals(expectedEvent, repository.event.value)
    }

    @Test
    fun `should work with blank tokens`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val loginTokens = MutableStateFlow(Tokens.BLANK)

        val expectedEvent = createMockEvent()

        coEvery { mockApi.getPostById(eventId) } returns expectedEvent

        val repository = EventRepository(
            scope = testScope,
            dispatcher = testDispatcher,
            api = mockApi,
            eventId = eventId,
            loginTokens = loginTokens
        )

        testScheduler.advanceUntilIdle()

        coVerify { mockApi.getPostById(eventId) }
        assertEquals(expectedEvent, repository.event.first())
    }
}