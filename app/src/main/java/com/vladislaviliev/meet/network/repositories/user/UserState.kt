package com.vladislaviliev.meet.network.repositories.user

internal sealed class UserState {
    object Loading : UserState()
    data class Connected(val user: User) : UserState()
    object Disconnected : UserState()

    fun getOrNull() = if (this is Connected) user else null
}