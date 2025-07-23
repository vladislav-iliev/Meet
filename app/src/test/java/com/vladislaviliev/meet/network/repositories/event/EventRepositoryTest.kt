package com.vladislaviliev.meet.network.repositories.event

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EventRepositoryTest {

    private val eventId = "test-event-id"
    private val mockApi = mockk<PostControllerApi>()

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
    fun `should handle ClientException gracefully when getting post by id`() = runTest {
        val mockPaginatedParticipants = createMockParticipantsResponse(emptyList())

        coEvery { mockApi.getPostById(eventId) } throws ClientException()
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } returns mockPaginatedParticipants

        val repository = EventRepository(coroutineContext[CoroutineDispatcher]!!, mockApi, eventId)
        repository.download()
        runCurrent()

        coVerify { mockApi.getPostById(eventId) }
        coVerify(exactly = 0) { mockApi.getPostParticipants(eventId, any(), any()) }

        val result = repository.event.first()
        assertEquals(Event.Blank, result)
    }

    @Test
    fun `should handle ClientException gracefully when getting participants`() = runTest {
        val mockPostDto = createMockEventDto()

        coEvery { mockApi.getPostById(eventId) } returns mockPostDto
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } throws ClientException(
            "Client error : 404 Not Found",
            404,
            mockk()
        )

        val repository = EventRepository(coroutineContext[CoroutineDispatcher]!!, mockApi, eventId)
        repository.download()
        runCurrent()

        coVerify { mockApi.getPostById(eventId) }
        coVerify { mockApi.getPostParticipants(eventId, 0, 10) }

        val result = repository.event.first()
        assertEquals(Event.Blank, result)
    }

    @Test
    fun `should initialize with blank event state`() = runTest {
        val repository = EventRepository(coroutineContext[CoroutineDispatcher]!!, mockk<PostControllerApi>(), eventId)
        assertEquals(Event.Blank, repository.event.value)
    }

    @Test
    fun `should update event state after successful API call`() = runTest {
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
        val expectedEvent = Event.Success(expectedPostDto, updatedParticipantPics)

        coEvery { mockApi.getPostById(eventId) } returns expectedPostDto
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } returns mockPaginatedParticipants

        val repository = EventRepository(coroutineContext[CoroutineDispatcher]!!, mockApi, eventId)
        repository.download()
        testScheduler.runCurrent()

        val result = repository.event.value
        assertTrue(result is Event.Success)
        assertEquals(expectedEvent.postResponseDto, result.postResponseDto)
        assertEquals(expectedEvent.participants, result.participants)
    }

    @Test
    fun `should work with blank tokens and empty participants`() = runTest {
        val expectedPostDto = createMockEventDto()
        val mockPaginatedParticipants = createMockParticipantsResponse(emptyList())
        val expectedEvent = Event.Success(expectedPostDto, emptyList())

        coEvery { mockApi.getPostById(eventId) } returns expectedPostDto
        coEvery { mockApi.getPostParticipants(eventId, 0, 10) } returns mockPaginatedParticipants


        val repository = EventRepository(coroutineContext[CoroutineDispatcher]!!, mockApi, eventId)
        repository.download()
        testScheduler.runCurrent()

        coVerify { mockApi.getPostById(eventId) }
        coVerify { mockApi.getPostParticipants(eventId, 0, 10) }

        val result = repository.event.first()
        assertTrue(result is Event.Success)
        assertEquals(expectedEvent.postResponseDto, result.postResponseDto)
        assertEquals(expectedEvent.participants, result.participants)
    }
}