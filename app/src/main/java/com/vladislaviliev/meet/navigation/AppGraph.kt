package com.vladislaviliev.meet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.createGraph
import kotlinx.serialization.Serializable

@Serializable
internal object AppGraphRoute

internal fun createAppGraph(controller: NavController) = controller
    .createGraph(LoginRoute, AppGraphRoute::class) { addAppGraphDestinations(controller) }

private fun NavGraphBuilder.addAppGraphDestinations(controller: NavController) {
    addSessionDestination(controller::onSessionRestarted)
    addLoginDestination(controller::onLoggedIn)
}