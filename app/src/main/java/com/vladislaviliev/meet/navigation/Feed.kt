package com.vladislaviliev.meet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vladislaviliev.meet.ui.feed.FeedScreen
import kotlinx.serialization.Serializable

@Serializable
object FeedRoute

internal fun NavGraphBuilder.addFeedDestination(onEventClick: (String) -> Unit) {
    composable<FeedRoute> { FeedScreen(onEventClick) }
}

internal fun NavController.onEventClick(id: String) {
    navigate(LoadEventRoute(id))
}