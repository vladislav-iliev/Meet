package com.vladislaviliev.meet.network

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.io.encoding.Base64

internal class TokenParser() {
    fun parse(token: String): Map<String, String> {
        val payload = token.split(".")[1]
        val decodedPayload = Base64.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(payload).decodeToString()
        return jacksonObjectMapper().readValue(decodedPayload)
    }

    fun parseExpiration(token: String) = parse(token)["exp"]?.toLong() ?: -1L
}
