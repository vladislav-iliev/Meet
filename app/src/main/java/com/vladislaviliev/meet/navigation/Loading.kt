package com.vladislaviliev.meet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vladislaviliev.meet.ui.loading.session.SessionScreen
import com.vladislaviliev.meet.ui.loading.user.LoadingUserScreen
import kotlinx.serialization.Serializable

@Serializable
object SessionRoute

@Serializable
object LoadUserRoute

internal fun NavGraphBuilder.addSessionDestination(onSessionRestarted: () -> Unit) {
    composable<SessionRoute> { SessionScreen(onSessionRestarted) }
}

internal fun NavGraphBuilder.addLoadingUserDestination(onLoaded: () -> Unit) {
    composable<LoadUserRoute> { LoadingUserScreen(onLoaded) }
}

internal fun NavController.onSessionRestarted() {
    popBackStack()
    navigate(LoginRoute)
}

internal fun NavController.onUserLoaded() {
    popBackStack()
    navigate(FeedRoute)
}