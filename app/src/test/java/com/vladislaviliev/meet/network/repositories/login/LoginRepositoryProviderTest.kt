package com.vladislaviliev.meet.network.repositories.login

import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.DefaultAsserter.assertNotEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoginRepositoryProviderTest {

    @Test
    fun `initial state should be null`() = runTest {
        assertNull(LoginRepositoryProvider().current.first())
    }

    @Test
    fun `update should change current repository`() = runTest {
        val provider = LoginRepositoryProvider()
        val mockRepository = mockk<LoginRepository>()

        provider.update(mockRepository)

        val currentValue = provider.current.first()
        assertEquals(mockRepository, currentValue)
    }

    @Test
    fun `update with null should reset repository`() = runTest {
        val provider = LoginRepositoryProvider()
        val mockRepository = mockk<LoginRepository>()

        provider.update(mockRepository)
        assertEquals(mockRepository, provider.current.first())

        provider.update(null)

        val currentValue = provider.current.first()
        assertNull(currentValue)
    }

    @Test
    fun `update should replace existing repository`() = runTest {
        val provider = LoginRepositoryProvider()
        val firstRepository = mockk<LoginRepository>()
        val secondRepository = mockk<LoginRepository>()

        provider.update(firstRepository)
        assertEquals(firstRepository, provider.current.first())

        provider.update(secondRepository)

        val currentValue = provider.current.first()
        assertEquals(secondRepository, currentValue)
        assertNotEquals(message = null, illegal = firstRepository, actual = currentValue)
    }

    @Test
    fun `current flow should be read-only`() {
        assertNotEquals(message = null, MutableStateFlow::class, LoginRepositoryProvider().current::class)
    }

    @Test
    fun `multiple updates should work correctly`() = runTest {
        val provider = LoginRepositoryProvider()
        val repo1 = mockk<LoginRepository>()
        val repo2 = mockk<LoginRepository>()
        val repo3 = mockk<LoginRepository>()

        provider.update(repo1)
        assertEquals(repo1, provider.current.first())

        provider.update(repo2)
        assertEquals(repo2, provider.current.first())

        provider.update(null)
        assertNull(provider.current.first())

        provider.update(repo3)
        assertEquals(repo3, provider.current.first())

        val finalValue = provider.current.first()
        assertEquals(repo3, finalValue)
    }
}