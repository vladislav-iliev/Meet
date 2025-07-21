package com.vladislaviliev.meet.koin

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.client.Client
import com.vladislaviliev.meet.network.repositories.login.LoginRepository
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryProvider
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryTimer
import com.vladislaviliev.meet.session.Session
import com.vladislaviliev.meet.session.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.Call
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.binds
import org.koin.dsl.module
import org.openapitools.client.apis.CognitoControllerApi

val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    singleOf(::LoginRepositoryProvider)
    single { SessionRepository(getKoin()) }

    singleOf(::TokenParser)

    single<OkHttpClient> { Client(get()).instance } binds arrayOf(Call.Factory::class)

    scope<Session> {
        scoped {
            LoginRepository(get(), Dispatchers.IO, CognitoControllerApi(client = get()), get())
                .also { get<LoginRepositoryProvider>().update(it) }
        }
        scoped {
            LoginRepositoryTimer(get(), get(), { System.currentTimeMillis() }, 60_000L)
        }
    }
}