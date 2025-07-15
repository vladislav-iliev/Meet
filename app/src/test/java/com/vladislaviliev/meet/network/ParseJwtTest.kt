package com.vladislaviliev.meet.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ParseJwtTest {
    @Test
    fun `parses correctly`() {
        val key =
            "eyJhbGciOiJIUzI1NiJ9.eyJSb2xlIjoiU29tZVJvbGUiLCJJc3N1ZXIiOiJTb21lSXNzdWVyIiwiVXNlcm5hbWUiOiJTb21lVXNlciIsImV4cCI6MTc1MjU5MDQ5MywiaWF0IjoxNzUyNTkwNDkzfQ.qVUCKg3inVZgupSH5rGj4zfr-BFooBqso8xEvVCgGMA"
        val decoded = parseJwt(key)
        assertEquals("SomeRole", decoded["Role"])
        assertEquals("SomeIssuer", decoded["Issuer"])
        assertEquals("SomeUser", decoded["Username"])
        assertEquals("1752590493", decoded["exp"])
        assertEquals("1752590493", decoded["iat"])
    }
}