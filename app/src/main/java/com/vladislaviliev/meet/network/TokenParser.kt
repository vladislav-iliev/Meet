package com.vladislaviliev.meet.network

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.io.encoding.Base64

internal class TokenParser() {
    fun parse(token: String): Map<String, String> {
        val payload = token.split(".")[1]
        val decodedPayload = String(Base64.withPadding(Base64.PaddingOption.ABSENT).decode(payload))
        return jacksonObjectMapper().readValue(decodedPayload)
    }
}
