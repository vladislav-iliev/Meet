package com.vladislaviliev.meet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vladislaviliev.meet.ui.loading.SessionScreen
import kotlinx.serialization.Serializable

@Serializable
object SessionRoute

internal fun NavGraphBuilder.addSessionDestination(onSessionRestarted: () -> Unit) {
    composable<SessionRoute> { SessionScreen(onSessionRestarted) }
}

internal fun NavController.onSessionRestarted() {
    popBackStack()
    navigate(LoginRoute)
}