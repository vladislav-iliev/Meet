package com.vladislaviliev.meet.network.repositories.login

import com.vladislaviliev.meet.network.Tokens
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginRepositoryTimerTest {

    private val tokensFlow = MutableStateFlow(Tokens.BLANK)
    private val repository = mockk<LoginRepository> {
        every { tokens } returns tokensFlow
        every { refreshSync() } answers { Result.success(Unit) }
    }

    @Test
    fun `should call refresh after timer runs `() = runTest {
        val eagerness = 500L
        val tokenExpiry = 1000L
        val advanceTimeBy = tokenExpiry - eagerness + 1L

        val currentTime = mockk<() -> Long> { every { this@mockk.invoke() } returnsMany listOf(0L, advanceTimeBy) }
        LoginRepositoryTimer(backgroundScope, repository, currentTime, eagerness)

        tokensFlow.value = Tokens("userId", "accessToken", "refreshToken", tokenExpiry)
        advanceTimeBy(advanceTimeBy)
        runCurrent()

        verify(exactly = 1) { repository.refreshSync() }
    }

    @Test
    fun `should not schedule refresh when tokens are blank`() = runTest {
        val currentTime = mockk<() -> Long> { every { this@mockk.invoke() } returns 0L }
        LoginRepositoryTimer(backgroundScope, repository, currentTime, 500L)

        advanceTimeBy(1000L)
        runCurrent()

        verify(exactly = 0) { repository.refreshSync() }
    }

    @Test
    fun `should schedule refresh when delay is zero`() = runTest {
        val currentTime = mockk<() -> Long> { every { this@mockk.invoke() } returns 500L }
        LoginRepositoryTimer(backgroundScope, repository, currentTime, 500L)

        tokensFlow.value = Tokens("userId", "accessToken", "refreshToken", 1000L)
        advanceTimeBy(1000L)
        runCurrent()

        verify(exactly = 1) { repository.refreshSync() }
    }

    @Test
    fun `should not schedule refresh when delay is negative`() = runTest {
        val currentTime = mockk<() -> Long> { every { this@mockk.invoke() } returns 600L }
        LoginRepositoryTimer(backgroundScope, repository, currentTime, 500L)

        tokensFlow.value = Tokens("userId", "accessToken", "refreshToken", 1000L)
        advanceTimeBy(1000L)
        runCurrent()

        verify(exactly = 0) { repository.refreshSync() }
    }

    @Test
    fun `should cancel previous refresh job when new tokens are received`() = runTest {
        val currentTime = mockk<() -> Long> { every { this@mockk.invoke() } returnsMany listOf(0L, 1000L, 2000L) }
        LoginRepositoryTimer(backgroundScope, repository, currentTime, 500L)

        tokensFlow.value = Tokens("userId1", "accessToken1", "refreshToken1", 2000L)
        advanceTimeBy(1000L)
        runCurrent()

        // Set new tokens - should cancel previous job
        tokensFlow.value = Tokens("userId2", "accessToken2", "refreshToken2", 3000L)
        advanceTimeBy(1000L) // Complete the time that would have triggered first refresh
        runCurrent()

        verify(exactly = 0) { repository.refreshSync() }

        advanceTimeBy(500L) // Advance to trigger the second refresh
        runCurrent()

        verify(exactly = 1) { repository.refreshSync() }
    }

    @Test
    fun `should cancel refresh job when tokens become blank`() = runTest {
        val currentTime = mockk<() -> Long> { every { this@mockk.invoke() } returns 0L }
        LoginRepositoryTimer(backgroundScope, repository, currentTime, 500L)

        tokensFlow.value = Tokens("userId", "accessToken", "refreshToken", 2000L)
        advanceTimeBy(1000L) // Advance but not enough to trigger refresh
        runCurrent()

        tokensFlow.value = Tokens.BLANK // Set tokens to blank - should cancel refresh job
        advanceTimeBy(2000L) // Advance past the original refresh time
        runCurrent()

        verify(exactly = 0) { repository.refreshSync() }
    }

    @Test
    fun `should not call refresh before delay time`() = runTest {
        val currentTime = mockk<() -> Long> { every { this@mockk.invoke() } returns 0L }

        LoginRepositoryTimer(backgroundScope, repository, currentTime, 500L)
        tokensFlow.value = Tokens("userId", "accessToken", "refreshToken", 2000L)

        advanceTimeBy(1499L) // Advance time but not enough to trigger refresh
        runCurrent()

        verify(exactly = 0) { repository.refreshSync() }
    }

    @Test
    fun `should handle multiple token updates correctly`() = runTest {
        val currentTime = mockk<() -> Long> { every { this@mockk.invoke() } returnsMany listOf(0L, 500, 1500L) }
        LoginRepositoryTimer(backgroundScope, repository, currentTime, 500L)

        tokensFlow.value = Tokens("userId1", "accessToken1", "refreshToken1", 1000L)
        advanceTimeBy(500L)
        runCurrent()

        verify(exactly = 1) { repository.refreshSync() }

        tokensFlow.value = Tokens("userId2", "accessToken2", "refreshToken2", 2000L)
        advanceTimeBy(1000L)
        runCurrent()

        verify(exactly = 2) { repository.refreshSync() }
    }
}