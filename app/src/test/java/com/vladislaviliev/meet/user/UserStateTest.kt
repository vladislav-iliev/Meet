package com.vladislaviliev.meet.user

import com.vladislaviliev.meet.network.repositories.user.User
import com.vladislaviliev.meet.network.repositories.user.UserState
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class UserStateTest {

    @Test
    fun `Loading state should be singleton`() {
        assertSame(UserState.Loading, UserState.Loading)
    }

    @Test
    fun `Disconnected state should be singleton`() {
        assertSame(UserState.Disconnected, UserState.Disconnected)
    }

    @Test
    fun `Connected state should hold user data`() {
        val mockUser = mockk<User>()
        assertEquals(mockUser, UserState.Connected(mockUser).user)
    }

    @Test
    fun `getOrNull returns user when Connected`() {
        val mockUser = mockk<User>()
        assertEquals(mockUser, UserState.Connected(mockUser).getOrNull())
    }

    @Test
    fun `getOrNull returns null when Loading`() {
        assertNull(UserState.Loading.getOrNull())
    }

    @Test
    fun `getOrNull returns null when Disconnected`() {
        assertNull(UserState.Disconnected.getOrNull())
    }

    @Test
    fun `Connected states with same user should be equal`() {
        val mockUser = mockk<User>()
        assertEquals(UserState.Connected(mockUser), UserState.Connected(mockUser))
    }

    @Test
    fun `Connected states with different users should not be equal`() {
        val mockUser1 = mockk<User>()
        val mockUser2 = mockk<User>()
        assertNotEquals(UserState.Connected(mockUser1), UserState.Connected(mockUser2))
    }
}