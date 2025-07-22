package com.vladislaviliev.meet.koin

import android.util.Log
import com.vladislaviliev.meet.network.repositories.login.LoginRepository
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryProvider
import com.vladislaviliev.meet.session.SessionRepository
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class LoginRepositoryProviderModuleTest : KoinTest {

    private lateinit var loginRepositoryProvider: LoginRepositoryProvider
    private lateinit var sessionRepository: SessionRepository

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.isLoggable(any(), any()) } returns false

        startKoin { modules(appModule) }
        loginRepositoryProvider = get()
        sessionRepository = get()
    }

    @After
    fun tearDown() {
        sessionRepository.endSession()
        stopKoin()

        unmockkStatic(Log::class)
    }

    @Test
    fun `LoginRepositoryProvider is properly configured as singleton`() {
        assertEquals(get<LoginRepositoryProvider>(), get<LoginRepositoryProvider>())
    }

    @Test
    fun `LoginRepositoryProvider starts with null current repository`() = runTest {
        assertNull(loginRepositoryProvider.current.first())
    }

    @Test
    fun `LoginRepository can be created from Session scope`() {
        sessionRepository.restartSession()
        val currentScope = sessionRepository.currentScope
        assertNotNull(currentScope)
        assertNotNull(currentScope.get<LoginRepository>())
    }

    @Test
    fun `Multiple LoginRepository instances are scoped correctly`() {
        // Given: First session
        sessionRepository.restartSession()
        val firstRepository = sessionRepository.currentScope?.get<LoginRepository>()

        // When: Ending session and starting new one
        sessionRepository.endSession()
        sessionRepository.restartSession()
        val secondRepository = sessionRepository.currentScope?.get<LoginRepository>()

        // Then: Should create different instances for different scopes
        assertNotNull(firstRepository)
        assertNotNull(secondRepository)
        assertNotSame(firstRepository, secondRepository)
    }

    @Test
    fun `LoginRepositoryProvider can be updated manually`() = runTest {
        // Given: Session is started and LoginRepository is created
        sessionRepository.restartSession()
        val loginRepository = sessionRepository.currentScope?.get<LoginRepository>()
        assertNotNull(loginRepository)

        // When: Manually updating the provider
        loginRepositoryProvider.update(loginRepository)

        // Then: Provider should contain the repository
        assertEquals(loginRepository, loginRepositoryProvider.current.first())
    }

    @Test
    fun `LoginRepositoryProvider is automatically updated when LoginRepository is created`() = runTest {
        // Given: Session is started
        sessionRepository.restartSession()
        val currentScope = sessionRepository.currentScope
        assertNotNull(currentScope)

        // Verify provider is initially null
        assertNull(loginRepositoryProvider.current.first())

        // When: LoginRepository is created from scope
        val loginRepository = currentScope.get<LoginRepository>()

        // Then: LoginRepositoryProvider should be automatically updated
        val updatedRepository = loginRepositoryProvider.current.first()
        assertNotNull(updatedRepository)
        assertEquals(loginRepository, updatedRepository)
    }

    @Test
    fun `LoginRepositoryProvider is automatically updated for each new session`() = runTest {
        // Given: First session
        sessionRepository.restartSession()
        val firstRepository = sessionRepository.currentScope?.get<LoginRepository>()

        // Then: Provider should be updated with first repository
        assertEquals(firstRepository, loginRepositoryProvider.current.first())

        // When: Ending session and starting new one
        sessionRepository.endSession()
        sessionRepository.restartSession()
        val secondRepository = sessionRepository.currentScope?.get<LoginRepository>()

        // Then: Provider should be automatically updated with new repository
        assertEquals(secondRepository, loginRepositoryProvider.current.first())
        assertNotSame(firstRepository, secondRepository)
    }

    @Test
    fun `LoginRepositoryProvider automatic update works with lazy initialization`() = runTest {
        // Given: Session is started but LoginRepository not yet requested
        sessionRepository.restartSession()
        val currentScope = sessionRepository.currentScope
        assertNotNull(currentScope)

        // Verify provider is still null (LoginRepository not created yet)
        assertNull(loginRepositoryProvider.current.first())

        // When: LoginRepository is lazily created on first access
        val loginRepository = currentScope.get<LoginRepository>()

        // Then: Provider should be immediately updated
        val updatedRepository = loginRepositoryProvider.current.first()
        assertNotNull(updatedRepository)
        assertEquals(loginRepository, updatedRepository)

        // Then: Should return the same instance and provider should remain unchanged
        assertEquals(loginRepository, currentScope.get<LoginRepository>())
        assertEquals(loginRepository, loginRepositoryProvider.current.first())
    }
}