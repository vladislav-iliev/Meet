package com.vladislaviliev.meet.ui.loading.user

import com.vladislaviliev.meet.network.repositories.user.UserRepository
import com.vladislaviliev.meet.ui.MainDispatcherRule
import com.vladislaviliev.meet.ui.loading.LoadingState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class LoadingUserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val userRepository = mockk<UserRepository>()

    @Test
    fun `initial state is Loading`() = runTest {
        Dispatchers.setMain(coroutineContext[CoroutineDispatcher]!!)

        coEvery { userRepository.download() } answers { Result.success(Unit) }
        val viewModel = LoadingUserViewModel(userRepository)
        assertEquals(LoadingState.Loading, viewModel.state.value)

        Dispatchers.resetMain()
    }


    @Test
    fun `state becomes Success when download is successful`() = runTest {
        coEvery { userRepository.download() } returns Result.success(Unit)
        val viewModel = LoadingUserViewModel(userRepository)
        assertEquals(LoadingState.Success, viewModel.state.value)
    }

    @Test
    fun `state becomes Error when download fails`() = runTest {
        val exception = RuntimeException()
        coEvery { userRepository.download() } returns Result.failure(exception)

        val viewModel = LoadingUserViewModel(userRepository)

        val value = viewModel.state.value
        assertTrue(value is LoadingState.Error)
        assertEquals(exception.toString(), (value as LoadingState.Error).message)
    }

    @Test
    fun `download function updates state to Success on successful retry`() = runTest {
        coEvery { userRepository.download() } returns Result.failure(RuntimeException("Initial error"))

        val viewModel = LoadingUserViewModel(userRepository)

        coEvery { userRepository.download() } returns Result.success(Unit)

        viewModel.download()
        assertEquals(LoadingState.Success, viewModel.state.value)
    }

    @Test
    fun `download function updates state to Error on failed retry`() = runTest {
        val exception1 = RuntimeException("1")
        val exception2 = RuntimeException("2")

        coEvery { userRepository.download() } returns Result.failure(exception1)

        val viewModel = LoadingUserViewModel(userRepository)

        coEvery { userRepository.download() } returns Result.failure(exception2)
        viewModel.download()

        val value = viewModel.state.value
        assertTrue(value is LoadingState.Error)
        assertEquals(exception2.toString(), (value as LoadingState.Error).message)
    }
}
