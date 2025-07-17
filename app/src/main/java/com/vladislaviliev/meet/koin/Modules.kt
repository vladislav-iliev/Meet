package com.vladislaviliev.meet.koin

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.client.Client
import com.vladislaviliev.meet.network.repositories.LoginRepository
import com.vladislaviliev.meet.network.repositories.LoginRepositoryProvider
import com.vladislaviliev.meet.session.Session
import com.vladislaviliev.meet.session.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.openapitools.client.apis.CognitoControllerApi

val appModule = module {
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }
    single<LoginRepositoryProvider> { LoginRepositoryProvider() }
    single<CognitoControllerApi> {
        val client = Client(get())
        CognitoControllerApi(client = client.instance)
    }
    singleOf(::TokenParser)
    single<SessionRepository> { SessionRepository(getKoin(), get()) }

    scope<Session> {
        scoped<LoginRepository> {
            LoginRepository(get<CoroutineScope>(), Dispatchers.IO, get(), get()).also {
                get<LoginRepositoryProvider>().update(it)
            }
        }
    }
}