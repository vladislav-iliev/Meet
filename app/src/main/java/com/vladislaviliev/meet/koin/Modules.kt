package com.vladislaviliev.meet.koin

import androidx.paging.PagingConfig
import com.vladislaviliev.meet.event.EventScope
import com.vladislaviliev.meet.event.EventScopeRepository
import com.vladislaviliev.meet.network.TokenParser
import com.vladislaviliev.meet.network.client.Client
import com.vladislaviliev.meet.network.repositories.event.EventRepository
import com.vladislaviliev.meet.network.repositories.feed.FeedRepository
import com.vladislaviliev.meet.network.repositories.login.LoginRepository
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryProvider
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryTimer
import com.vladislaviliev.meet.network.repositories.user.UserRepository
import com.vladislaviliev.meet.session.Session
import com.vladislaviliev.meet.session.SessionRepository
import com.vladislaviliev.meet.ui.event.EventViewModel
import com.vladislaviliev.meet.ui.feed.FeedViewModel
import com.vladislaviliev.meet.ui.loading.event.LoadingEventViewModel
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
import org.koin.dsl.binds
import org.koin.dsl.module
import org.openapitools.client.apis.CognitoControllerApi
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.apis.UserControllerApi

val appModule = module {
    single { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    singleOf(::LoginRepositoryProvider)

    single { SessionRepository(getKoin()) }
    single { EventScopeRepository(getKoin()) }

    singleOf(::TokenParser)

    single<OkHttpClient> { Client(get()).instance } binds arrayOf(Call.Factory::class)

    viewModelOf(::SessionViewModel)

    viewModel { LoginViewModel(get<SessionRepository>().currentScope!!.get<LoginRepository>()) }

    viewModel { LoadingUserViewModel(get<SessionRepository>().currentScope!!.get<UserRepository>()) }

    viewModel {
        val pagingConfig = PagingConfig(10, enablePlaceholders = false)
        FeedViewModel(get<SessionRepository>().currentScope!!.get<FeedRepository>(), pagingConfig)
    }

    scope<Session> {
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
        scoped { FeedRepository(Dispatchers.IO, PostControllerApi(client = get()), get<UserRepository>().user.value) }
    }

    scope<EventScope> {
        scoped {
            EventRepository(
                Dispatchers.IO, PostControllerApi(client = get()), get<EventScopeRepository>().currentEventId!!
            )
        }
        scoped { LoadingEventViewModel(get<EventScopeRepository>().currentScope!!.get<EventRepository>()) }
        scoped { EventViewModel(get<EventScopeRepository>().currentScope!!.get<EventRepository>()) }
    }
}