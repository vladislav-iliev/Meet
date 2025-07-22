package com.vladislaviliev.meet.ui.loading.session

import com.vladislaviliev.meet.session.SessionRepository
import com.vladislaviliev.meet.ui.MainDispatcherRule
import com.vladislaviliev.meet.ui.loading.LoadingState
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private val mockSessionRepository = mockk<SessionRepository>(relaxed = true)

    @Test
    fun `init calls restartSession on repository`() = runTest {
        SessionViewModel(mockSessionRepository)
        verify { mockSessionRepository.restartSession() }
    }

    @Test
    fun `init sets state to success`() = runTest {
        assertEquals(LoadingState.Success, SessionViewModel(mockSessionRepository).state.value)
    }
}