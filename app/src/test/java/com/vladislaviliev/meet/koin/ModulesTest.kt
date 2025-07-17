package com.vladislaviliev.meet.koin

import com.vladislaviliev.meet.session.SessionRepository
import org.junit.Test
import org.koin.core.Koin
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
class ModulesTest {

    @Test
    fun `verify appModule configuration`() {
        appModule.verify(
            injections = injectedParameters(definition<SessionRepository>(Koin::class))
        )
    }
}