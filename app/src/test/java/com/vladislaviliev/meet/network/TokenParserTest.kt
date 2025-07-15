package com.vladislaviliev.meet.network

import org.junit.Assert.assertEquals
import org.junit.Test

class TokenParserTest {
    @Test
    fun `parses correctly`() {
        val key =
            "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiU29tZVJvbGUiLCJJc3N1ZXIiOiJTb21lSXNzdWVyIiwiVXNlcm5hbWUiOiJTb21lVXNlciIsImV4cCI6MTc1MjU5MDQ5MywiaWF0IjoxNzUyNTkwNDkzfQ.qVUCKg3inVZgupSH5rGj4zfr-BFooBqso8xEvVCgGMA"
        val decoded = TokenParser().parse(key)
        assertEquals("SomeRole", decoded["Role"])
        assertEquals("SomeIssuer", decoded["Issuer"])
        assertEquals("SomeUser", decoded["Username"])
        assertEquals("1752590493", decoded["exp"])
        assertEquals("1752590493", decoded["iat"])
    }

    @Test
    fun `parses expiration correctly`() {
        val key =
            "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiU29tZVJvbGUiLCJJc3N1ZXIiOiJTb21lSXNzdWVyIiwiVXNlcm5hbWUiOiJTb21lVXNlciIsImV4cCI6MTc1MjU5MDQ5MywiaWF0IjoxNzUyNTkwNDkzfQ.qVUCKg3inVZgupSH5rGj4zfr-BFooBqso8xEvVCgGMA"
        assertEquals(1752590493L, TokenParser().parseExpiration(key))
    }
}