package com.vladislaviliev.meet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vladislaviliev.meet.ui.event.EventScreen
import kotlinx.serialization.Serializable

@Serializable
object EventRoute

internal fun NavGraphBuilder.addEventDestination(onUpPressed: () -> Unit) {
    composable<EventRoute> { EventScreen(onUpPressed) }
}

internal fun NavController.onEventUpPressed() {
    popBackStack()
}