package com.vladislaviliev.meet.network

internal data class Tokens(val userId: String, val access: String, val refresh: String, val expiry: Long) {
    val isBlank = access.isBlank()

    companion object {
        val BLANK = Tokens("", "", "", -1)
    }
}
