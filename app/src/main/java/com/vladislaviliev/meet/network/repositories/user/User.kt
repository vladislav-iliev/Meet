package com.vladislaviliev.meet.network.repositories.user

internal data class User(val latitude: Double, val longitude: Double) {
    companion object {
        val BLANK = User(0.0, 0.0)
    }
}