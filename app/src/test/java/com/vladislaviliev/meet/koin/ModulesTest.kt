package com.vladislaviliev.meet.koin

import com.vladislaviliev.meet.network.repositories.login.LoginRepository
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryTimer
import com.vladislaviliev.meet.network.repositories.user.UserRepository
import com.vladislaviliev.meet.session.SessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.koin.core.Koin
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import org.openapitools.client.apis.CognitoControllerApi
import org.openapitools.client.apis.UserControllerApi

@OptIn(KoinExperimentalAPI::class)
class ModulesTest {

    @Test
    fun `verify appModule configuration`() {
        appModule.verify(
            injections = injectedParameters(
                definition<SessionRepository>(Koin::class),
                definition<LoginRepository>(CoroutineDispatcher::class, CognitoControllerApi::class),
                definition<LoginRepositoryTimer>(Function0::class, Long::class),
                definition<UserRepository>(CoroutineDispatcher::class, UserControllerApi::class, String::class),
            )
        )
    }
}