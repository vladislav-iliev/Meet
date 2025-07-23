package com.vladislaviliev.meet.koin

import androidx.paging.PagingConfig
import com.vladislaviliev.meet.event.EventScopeRepository
import com.vladislaviliev.meet.network.repositories.event.EventRepository
import com.vladislaviliev.meet.network.repositories.feed.FeedRepository
import com.vladislaviliev.meet.network.repositories.login.LoginRepository
import com.vladislaviliev.meet.network.repositories.login.LoginRepositoryTimer
import com.vladislaviliev.meet.network.repositories.user.User
import com.vladislaviliev.meet.network.repositories.user.UserRepository
import com.vladislaviliev.meet.session.SessionRepository
import com.vladislaviliev.meet.ui.feed.FeedViewModel
import kotlinx.coroutines.CoroutineDispatcher
import org.junit.Test
import org.koin.core.Koin
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.verify.definition
import org.koin.test.verify.injectedParameters
import org.koin.test.verify.verify
import org.openapitools.client.apis.CognitoControllerApi
import org.openapitools.client.apis.PostControllerApi
import org.openapitools.client.apis.UserControllerApi

@OptIn(KoinExperimentalAPI::class)
class ModulesTest {

    @Test
    fun `verify appModule configuration`() {
        appModule.verify(
            injections = injectedParameters(
                definition<SessionRepository>(Koin::class),
                definition<EventScopeRepository>(Koin::class),
                definition<FeedViewModel>(PagingConfig::class),
                definition<LoginRepository>(CoroutineDispatcher::class, CognitoControllerApi::class),
                definition<LoginRepositoryTimer>(Function0::class, Long::class),
                definition<UserRepository>(CoroutineDispatcher::class, UserControllerApi::class, String::class),
                definition<FeedRepository>(CoroutineDispatcher::class, PostControllerApi::class, User::class),
                definition<EventRepository>(CoroutineDispatcher::class, PostControllerApi::class, String::class),
            )
        )
    }
}