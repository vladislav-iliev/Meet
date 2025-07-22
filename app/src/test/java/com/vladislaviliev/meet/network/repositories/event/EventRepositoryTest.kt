package com.vladislaviliev.meet.network.repositories.event

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.infrastructure.ClientException
import org.openapitools.client.models.BaseLocation
import org.openapitools.client.models.ExtendedMiniUser
import org.openapitools.client.models.ListResponseDtoExtendedMiniUser
import org.openapitools.client.models.MiniUser
import org.openapitools.client.models.PostResponseDto
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EventRepositoryTest {

    private val eventId = "test-event-id"
    private val specificEventId = "specific-event-123"
    private val mockParticipantProfilePics = listOf("pic1.jpg", "pic2.jpg")

    private fun createMockEventDto(
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
        location = mockk<BaseLocation>(),
        createdAt = OffsetDateTime.now(),
        interests = emptySet(),
        owner = mockk<MiniUser>(),
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

    private fun createMockParticipantsResponse(profilePics: List<String>): ListResponseDtoExtendedMiniUser {
        val extendedMiniUsers = profilePics.mapIndexed { index, pic ->
            val mockMiniUser = mockk<MiniUser>()
            every { mockMiniUser.profilePhotos } returns listOf(profilePics[index])
            mockk<ExtendedMiniUser> { every { user } returns mockMiniUser }
        }
        return ListResponseDtoExtendedMiniUser(extendedMiniUsers, true, extendedMiniUsers.size.toLong())
    }

    @Test
    fun `should load event successfully on initialization`() = runTest {
        val mockApi = mockk<PostControllerApi>()

        val expectedPostDto = createMockEventDto(
            title = "Test Event",
            images = listOf("image1.jpg", "image2.jpg"),
            payment = 25.0,
            needsLocationalConfirmation = true,
            participantsCount = 5,
            description = "Test Event Description"
        )
        val mockPaginatedParticipants = createMockParticipantsResponse(mockParticipantProfilePics)
        val expectedEventResponse = EventResponse(expectedPostDto, mockParticipantProfilePics)

        coEvery { mockApi.getPostById(eventId) } returns expectedPostDto
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } returns mockPaginatedParticipants

        val repository = EventRepository(
            scope = backgroundScope,
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            eventId = eventId,
        )

        testScheduler.runCurrent()
        repository.event.dropWhile { it == null }.first()

        coVerify { mockApi.getPostById(eventId) }
        coVerify { mockApi.getPostParticipants(eventId, 0, 10) }

        val result = repository.event.first()
        assertNotNull(result)
        assertTrue(result.isSuccess)
        assertEquals(expectedEventResponse, result.getOrNull())
    }

    @Test
    fun `should handle ClientException gracefully when getting post by id`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val mockPaginatedParticipants = createMockParticipantsResponse(emptyList())


        coEvery { mockApi.getPostById(eventId) } throws ClientException("Client error : 404 Not Found", 404, mockk())
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } returns mockPaginatedParticipants


        val repository = EventRepository(
            scope = backgroundScope,
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            eventId = eventId,
        )

        testScheduler.runCurrent()
        repository.event.dropWhile { it == null }.first()

        coVerify { mockApi.getPostById(eventId) }
        coVerify(exactly = 0) { mockApi.getPostParticipants(eventId, any(), any()) }

        val result = repository.event.first()
        assertNotNull(result)
        assertTrue(result.isFailure)
        assertNull(result.getOrNull())
    }

    @Test
    fun `should handle ClientException gracefully when getting participants`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val mockPostDto = createMockEventDto()

        coEvery { mockApi.getPostById(eventId) } returns mockPostDto
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } throws ClientException(
            "Client error : 404 Not Found",
            404,
            mockk()
        )

        val repository = EventRepository(
            scope = backgroundScope,
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            eventId = eventId,
        )

        testScheduler.runCurrent()
        repository.event.dropWhile { it == null }.first()

        coVerify { mockApi.getPostById(eventId) }
        coVerify { mockApi.getPostParticipants(eventId, 0, 10) }

        val result = repository.event.first()
        assertNotNull(result)
        assertTrue(result.isFailure)
        assertNull(result.getOrNull())
    }

    @Test
    fun `should call API with correct event ID`() = runTest {
        val mockApi = mockk<PostControllerApi>()
        val mockPostDto = createMockEventDto(id = specificEventId, title = "Mock Event")
        val mockPaginatedParticipants = createMockParticipantsResponse(mockParticipantProfilePics)

        coEvery { mockApi.getPostById(specificEventId) } returns mockPostDto
        coEvery { mockApi.getPostParticipants(specificEventId, 0, 10) } returns mockPaginatedParticipants

        val repository = EventRepository(
            scope = backgroundScope,
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            eventId = specificEventId,
        )

        testScheduler.runCurrent()
        repository.event.dropWhile { it == null }.first()

        coVerify { mockApi.getPostById(specificEventId) }
        coVerify { mockApi.getPostParticipants(specificEventId, 0, 10) }
    }

    @Test
    fun `should initialize with null event state before load`() = runTest {
        val mockApi = mockk<PostControllerApi>() // Mock behavior not strictly needed for this initial state check


        val mockPostDto = createMockEventDto()
        val mockPaginatedParticipants = createMockParticipantsResponse(emptyList())
        coEvery { mockApi.getPostById(eventId) } returns mockPostDto
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } returns mockPaginatedParticipants

        val repository = EventRepository(
            scope = backgroundScope,
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            eventId = eventId,
        )
        assertNull(repository.event.value)
    }

    @Test
    fun `should update event state after successful API call`() = runTest {
        val mockApi = mockk<PostControllerApi>()

        val expectedPostDto = createMockEventDto(
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
        val updatedParticipantPics = listOf("updated_pic.jpg")
        val mockPaginatedParticipants = createMockParticipantsResponse(updatedParticipantPics)
        val expectedEventResponse = EventResponse(expectedPostDto, updatedParticipantPics)

        coEvery { mockApi.getPostById(eventId) } returns expectedPostDto
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } returns mockPaginatedParticipants

        val repository = EventRepository(
            scope = backgroundScope,
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            eventId = eventId,
        )

        assertNull(repository.event.value)
        testScheduler.runCurrent()
        repository.event.dropWhile { it == null }.first()

        val result = repository.event.value
        assertNotNull(result)
        assertTrue(result.isSuccess)
        assertEquals(expectedEventResponse, result.getOrNull())
    }

    @Test
    fun `should work with blank tokens and empty participants`() = runTest {
        val mockApi = mockk<PostControllerApi>()

        val expectedPostDto = createMockEventDto()
        val mockPaginatedParticipants = createMockParticipantsResponse(emptyList())
        val expectedEventResponse = EventResponse(expectedPostDto, emptyList())

        coEvery { mockApi.getPostById(eventId) } returns expectedPostDto
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } returns mockPaginatedParticipants


        val repository = EventRepository(
            scope = backgroundScope,
            dispatcher = coroutineContext[CoroutineDispatcher]!!,
            api = mockApi,
            eventId = eventId,
        )

        testScheduler.runCurrent()
        repository.event.dropWhile { it == null }.first()

        coVerify { mockApi.getPostById(eventId) }
        coVerify { mockApi.getPostParticipants(eventId, 0, 10) }

        val result = repository.event.first()
        assertNotNull(result)
        assertTrue(result.isSuccess)
        assertEquals(expectedEventResponse, result.getOrNull())
    }
}