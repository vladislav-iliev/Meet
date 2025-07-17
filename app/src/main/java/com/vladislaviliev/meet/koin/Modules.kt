package com.vladislaviliev.meet.koin

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.client.Client
import com.vladislaviliev.meet.network.repositories.LoginRepository
import com.vladislaviliev.meet.network.repositories.LoginRepositoryProvider
import com.vladislaviliev.meet.network.repositories.LoginRepositoryTimer
import com.vladislaviliev.meet.session.Session
import com.vladislaviliev.meet.session.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.openapitools.client.apis.CognitoControllerApi

val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    singleOf(::LoginRepositoryProvider)
    single { CognitoControllerApi(client = Client(get()).instance) }
    singleOf(::TokenParser)
    single { SessionRepository(getKoin(), get()) }

    scope<Session> {
        scoped { LoginRepository(get(), get()).also { get<LoginRepositoryProvider>().update(it) } }
        scoped {
            LoginRepositoryTimer(get(), get(), { System.currentTimeMillis() }, 60_000L)
        }
    }
}