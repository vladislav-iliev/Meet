package com.vladislaviliev.meet.network

internal data class Tokens(val access: String, val refresh: String, val expiration: Long) {
    val isBlank = access.isNotBlank()

    companion object {
        val BLANK = Tokens("", "", -1)
    }
}
