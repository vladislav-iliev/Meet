package com.vladislaviliev.meet.koin

import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryTimer
import com.vladislaviliev.meet.session.SessionRepository
import junit.framework.TestCase.assertFalse
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class SessionScopeTest : KoinTest {

    private lateinit var sessionRepository: SessionRepository

    @Before
    fun setUp() {
        startKoin { modules(appModule) }
        sessionRepository = get()
    }

    @After
    fun tearDown() {
        sessionRepository.endSession()
        stopKoin()
    }

    @Test
    fun `LoginRepositoryTimer can be instantiated via Session scope`() {
        // Given: A session is started
        sessionRepository.restartSession()

        // When: We try to get LoginRepositoryTimer from the session scope
        val currentScope = sessionRepository.currentScope
        assertNotNull(currentScope)

        // Then: LoginRepositoryTimer should be successfully instantiated
        assertNotNull(currentScope.get<LoginRepositoryTimer>())
    }

    @Test
    fun `LoginRepositoryTimer is scoped to Session lifecycle`() {
        // Given: A session is started
        sessionRepository.restartSession()
        val firstTimer = sessionRepository.currentScope?.get<LoginRepositoryTimer>()

        // When: Session is ended and restarted
        sessionRepository.endSession()
        sessionRepository.restartSession()
        val secondTimer = sessionRepository.currentScope?.get<LoginRepositoryTimer>()

        // Then: A new instance should be created for the new scope
        assertNotNull(firstTimer)
        assertNotNull(secondTimer)
        assertNotSame(firstTimer, secondTimer)
    }

    @Test
    fun `Session scope is properly managed by SessionRepository`() {
        // Given: No active session
        assertTrue(!sessionRepository.isSessionActive.value)

        // When: Session is started
        sessionRepository.restartSession()

        // Then: Session should be active and scope should be available
        assertTrue(sessionRepository.isSessionActive.value)
        assertNotNull(sessionRepository.currentScope)

        // When: Session is ended
        sessionRepository.endSession()

        // Then: Session should be inactive
        assertFalse(sessionRepository.isSessionActive.value)
    }
}