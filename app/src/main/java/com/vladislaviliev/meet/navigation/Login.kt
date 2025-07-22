package com.vladislaviliev.meet.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vladislaviliev.meet.ui.login.LoginScreen
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

internal fun NavGraphBuilder.addLoginDestination(onLoggedIn: () -> Unit) {
    composable<LoginRoute> { LoginScreen(onLoggedIn) }
}

internal fun NavController.onLoggedIn() {
    println()
    println("success")
    println("success")
    println("success")
    println("success")
    println("success")
}