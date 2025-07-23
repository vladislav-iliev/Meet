package com.vladislaviliev.meet.ui.loading.event

import com.vladislaviliev.meet.network.repositories.event.EventRepository
import com.vladislaviliev.meet.ui.MainDispatcherRule
import com.vladislaviliev.meet.ui.loading.LoadingState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoadingEventViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val eventRepository = mockk<EventRepository>()

    @Test
    fun `initial state is Loading`() = runTest {
        Dispatchers.setMain(coroutineContext[CoroutineDispatcher]!!)

        coEvery { eventRepository.download() } answers { Result.success(Unit) }
        val viewModel = LoadingEventViewModel(eventRepository)
        assertEquals(LoadingState.Loading, viewModel.state.value)

        Dispatchers.resetMain()
    }


    @Test
    fun `state becomes Success when download is successful`() = runTest {
        coEvery { eventRepository.download() } returns Result.success(Unit)
        val viewModel = LoadingEventViewModel(eventRepository)
        assertEquals(LoadingState.Success, viewModel.state.value)
    }

    @Test
    fun `state becomes Error when download fails`() = runTest {
        val exception = RuntimeException()
        coEvery { eventRepository.download() } returns Result.failure(exception)

        val viewModel = LoadingEventViewModel(eventRepository)

        val finalState = viewModel.state.value
        assertTrue(finalState is LoadingState.Error)
        assertEquals(exception.toString(), (finalState as LoadingState.Error).message)
    }

    @Test
    fun `download function updates state to Success on successful retry`() = runTest {
        coEvery { eventRepository.download() } returns Result.failure(RuntimeException("Initial error"))

        val viewModel = LoadingEventViewModel(eventRepository)
        viewModel.state.first { it is LoadingState.Error } // Wait for initial error

        coEvery { eventRepository.download() } returns Result.success(Unit)

        viewModel.download() // Retry download
        assertEquals(LoadingState.Success, viewModel.state.value)
    }

    @Test
    fun `download function updates state to Error on failed retry`() = runTest {
        val exception1 = RuntimeException("Initial error")
        val exception2 = RuntimeException("Retry error")

        coEvery { eventRepository.download() } returns Result.failure(exception1)

        val viewModel = LoadingEventViewModel(eventRepository)
        viewModel.state.first { it is LoadingState.Error } // Wait for initial error

        coEvery { eventRepository.download() } returns Result.failure(exception2)
        viewModel.download() // Retry download

        val value = viewModel.state.value
        assertTrue(value is LoadingState.Error)
        assertEquals(exception2.toString(), (value as LoadingState.Error).message)
    }
}
