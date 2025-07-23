package com.vladislaviliev.meet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.vladislaviliev.meet.ui.loading.event.LoadingEventScreen
import com.vladislaviliev.meet.ui.loading.session.SessionScreen
import com.vladislaviliev.meet.ui.loading.user.LoadingUserScreen
import kotlinx.serialization.Serializable

@Serializable
object SessionRoute

@Serializable
object LoadUserRoute

@Serializable
data class LoadEventRoute(val eventId: String)

internal fun NavGraphBuilder.addSessionDestination(onSessionRestarted: () -> Unit) {
    composable<SessionRoute> { SessionScreen(onSessionRestarted) }
}

internal fun NavGraphBuilder.addLoadingUserDestination(onLoaded: () -> Unit) {
    composable<LoadUserRoute> { LoadingUserScreen(onLoaded) }
}

internal fun NavGraphBuilder.addLoadingEventDestination(onLoaded: () -> Unit) {
    composable<LoadEventRoute> { backStackEntry ->
        LoadingEventScreen(onLoaded, backStackEntry.toRoute<LoadEventRoute>().eventId)
    }
}

internal fun NavController.onSessionRestarted() {
    popBackStack()
    navigate(LoginRoute)
}

internal fun NavController.onUserLoaded() {
    popBackStack()
    navigate(FeedRoute)
}

internal fun NavController.onEventLoaded() {
    popBackStack()
    navigate(EventRoute)
}