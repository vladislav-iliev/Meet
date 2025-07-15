package com.vladislaviliev.meet.network

import okhttp3.Request

internal const val HEADER_AUTH_KEY = "Authorization"
internal const val HEADER_AUTH_VALUE = "Bearer %1\$s"

fun Request.sign(token: String) = newBuilder()
    .header(HEADER_AUTH_KEY, String.format(HEADER_AUTH_VALUE, token))
    .build()