package com.vladislaviliev.meet.network.repositories.user

import com.vladislaviliev.meet.network.Tokens
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.openapitools.client.apis.UserControllerApi
import org.openapitools.client.models.BaseLocation
import org.openapitools.client.models.UserInfoDto
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryTest {

    @Test
    fun `init should call load and start with null user`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 10.0
        every { location.longitude } returns 20.0
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, api, loginTokens)
        assertNull(userRepository.user.value)

        runCurrent()
        coVerify { api.getUserInfo("userId") }
        assertNotNull(userRepository.user.value)
    }

    @Test
    fun `load should set user when API call succeeds`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 15.5
        every { location.longitude } returns 25.5
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, api, loginTokens)
        runCurrent()

        val user = userRepository.user.value
        assertNotNull(user)
        assertEquals(15.5, user.latitude)
        assertEquals(25.5, user.longitude)
    }

    @Test
    fun `load should keep user null when API call fails`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        coEvery { api.getUserInfo("userId") } throws RuntimeException("API Error")

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, api, loginTokens)
        runCurrent()
        assertNull(userRepository.user.value)
    }

    @Test
    fun `loginTokens should expose the provided tokens flow`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 10.0
        every { location.longitude } returns 20.0
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, api, loginTokens)

        assertEquals(loginTokens.value, userRepository.loginTokens.value)
        assertEquals("userId", userRepository.loginTokens.value.userId)
        assertEquals("access", userRepository.loginTokens.value.access)
    }

    @Test
    fun `downloadInfo should return User with correct coordinates`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 50.0
        every { location.longitude } returns 60.0
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, api, loginTokens)
        runCurrent()

        val user = userRepository.user.value
        assertNotNull(user)
        assertEquals(50.0, user.latitude)
        assertEquals(60.0, user.longitude)
    }

    @Test
    fun `token changes should be reflected in loginTokens flow`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 10.0
        every { location.longitude } returns 20.0
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo(any()) } returns userInfoDto

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, api, loginTokens)
        runCurrent()

        loginTokens.value = Tokens("newUserId", "newAccess", "newRefresh", 987654321)

        val newTokens = userRepository.loginTokens.first()
        assertEquals("newUserId", newTokens.userId)
        assertEquals("newAccess", newTokens.access)
        assertEquals("newRefresh", newTokens.refresh)
        assertEquals(987654321, newTokens.expiry)
    }
}