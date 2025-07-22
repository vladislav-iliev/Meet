package com.vladislaviliev.meet.koin

import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.client.Client
import com.vladislaviliev.meet.network.repositories.login.LoginRepository
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryProvider
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryTimer
import com.vladislaviliev.meet.network.repositories.user.UserRepository
import com.vladislaviliev.meet.session.Session
import com.vladislaviliev.meet.session.SessionRepository
import com.vladislaviliev.meet.ui.loading.session.SessionViewModel
import com.vladislaviliev.meet.ui.loading.user.LoadingUserViewModel
import com.vladislaviliev.meet.ui.login.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.Call
import okhttp3.OkHttpClient
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module
import org.openapitools.client.apis.CognitoControllerApi
import org.openapitools.client.apis.UserControllerApi

val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    singleOf(::LoginRepositoryProvider)
    single { SessionRepository(getKoin()) }

    singleOf(::TokenParser)

    single<OkHttpClient> { Client(get()).instance } binds arrayOf(Call.Factory::class)

    viewModelOf(::SessionViewModel)

    viewModel {
        LoginViewModel(get<SessionRepository>().currentScope!!.get<LoginRepository>())
    }

    viewModel {
        LoadingUserViewModel(get<SessionRepository>().currentScope!!.get<UserRepository>())
    }

    scope(named<Session>()) {
        scoped {
            LoginRepository(Dispatchers.IO, CognitoControllerApi(client = get()), get())
                .also { get<LoginRepositoryProvider>().update(it) }
        }
        scoped {
            LoginRepositoryTimer(get(), get(), { System.currentTimeMillis() }, 60_000L)
        }
        scoped {
            UserRepository(
                Dispatchers.IO,
                UserControllerApi(client = get()),
                get<LoginRepository>().tokens.value.userId,
            )
        }
    }
}