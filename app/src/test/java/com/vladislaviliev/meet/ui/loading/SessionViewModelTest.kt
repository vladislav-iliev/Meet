package com.vladislaviliev.meet.ui.loading

import com.vladislaviliev.meet.session.SessionRepository
import com.vladislaviliev.meet.ui.MainDispatcherRule
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sessionStateFlow = MutableStateFlow(false)
    private val mockSessionRepository = mockk<SessionRepository>(relaxed = true) {
        every { isSessionActive } returns sessionStateFlow
        every { endSession() } answers { sessionStateFlow.value = false }
        every { restartSession() } answers { sessionStateFlow.value = true }
    }

    @Test
    fun `init calls restartSession on repository`() = runTest {
        SessionViewModel(mockSessionRepository)
        verify { mockSessionRepository.restartSession() }
    }

    @Test
    fun `isSessionActive reflects repository state after initial false and dropWhile`() = runTest {
        every { mockSessionRepository.restartSession() } just Runs

        val viewModel = SessionViewModel(mockSessionRepository)
        assertFalse(viewModel.isSessionActive.value)

        mockSessionRepository.endSession()
        assertFalse(viewModel.isSessionActive.value)

        every { mockSessionRepository.restartSession() } answers { sessionStateFlow.value = true }
        mockSessionRepository.restartSession()
        assertTrue(viewModel.isSessionActive.value)
    }


    @Test
    fun `isSessionActive becomes false when repository session ends`() = runTest {
        val viewModel = SessionViewModel(mockSessionRepository)

        assertTrue(viewModel.isSessionActive.value)

        mockSessionRepository.endSession()
        assertFalse(viewModel.isSessionActive.value)
    }

    @Test
    fun `isSessionActive correctly skips initial emissions`() = runTest {
        every { mockSessionRepository.restartSession() } just Runs

        val viewModel = SessionViewModel(mockSessionRepository)
        assertFalse(viewModel.isSessionActive.value)

        every { mockSessionRepository.restartSession() } answers { sessionStateFlow.value = true }
        // 1. Repository emits `false` (initial value of _isSessionActive)
        //    ViewModel's flow: drop(1) skips this.
        //    ViewModel remains `false`.
        mockSessionRepository.endSession()
        assertFalse(viewModel.isSessionActive.value)

        // 2. Repository emits `false` again (simulating some intermediate state)
        //    ViewModel's flow: dropWhile { !it } will drop this `false`.
        //    ViewModel remains `false`.
        mockSessionRepository.endSession()
        assertFalse(viewModel.isSessionActive.value)


        // 3. Repository emits `true`
        //    ViewModel's flow: dropWhile { !it } now lets this `true` pass.
        //    ViewModel becomes `true`.
        mockSessionRepository.restartSession()
        assertTrue(viewModel.isSessionActive.value)

        // 4. Repository emits `false` again
        //    ViewModel's flow: This `false` passes.
        //    ViewModel becomes `false`.
        mockSessionRepository.endSession()
        assertFalse(viewModel.isSessionActive.value)
    }
}