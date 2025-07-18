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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryTest {

    @Test
    fun `init should call load and set initial state to Loading`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 10.0
        every { location.longitude } returns 20.0
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository =
            UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, loginTokens, api)
        assertEquals(UserState.Loading, userRepository.userState.value)

        runCurrent()
        coVerify { api.getUserInfo("userId") }
        assertTrue(userRepository.userState.value is UserState.Connected)
    }

    @Test
    fun `load should set Connected state when API call succeeds`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 15.5
        every { location.longitude } returns 25.5
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, loginTokens, api)
        runCurrent()

        val state = userRepository.userState.value
        assertTrue(state is UserState.Connected)
        assertEquals(15.5, state.user.latitude)
        assertEquals(25.5, state.user.longitude)
    }

    @Test
    fun `load should set Disconnected state when API call fails`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        coEvery { api.getUserInfo("userId") } throws RuntimeException("API Error")

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, loginTokens, api)
        runCurrent()
        assertEquals(UserState.Disconnected, userRepository.userState.value)
    }

    @Test
    fun `collectLoginTokens should set Disconnected when tokens are blank`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 10.0
        every { location.longitude } returns 20.0
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, loginTokens, api)
        runCurrent()

        loginTokens.value = Tokens.Companion.BLANK
        userRepository.userState.first { it is UserState.Disconnected }
    }

    @Test
    fun `collectLoginTokens should set Connected when tokens are not blank`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 30.0
        every { location.longitude } returns 40.0
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, loginTokens, api)
        runCurrent()

        loginTokens.value = Tokens.Companion.BLANK
        userRepository.userState.first { it is UserState.Disconnected }
        assertEquals(UserState.Disconnected, userRepository.userState.value)

        loginTokens.value = Tokens("newUserId", "newAccess", "newRefresh", 987654321)
        userRepository.userState.first { it is UserState.Connected }
        val state = userRepository.userState.value
        assertTrue(state is UserState.Connected)
        assertEquals(30.0, state.user.latitude)
        assertEquals(40.0, state.user.longitude)
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

        val userRepository = UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, loginTokens, api)
        runCurrent()

        userRepository.userState.first { it is UserState.Connected }
        val state = userRepository.userState.value
        assertTrue(state is UserState.Connected)
        assertEquals(50.0, state.user.latitude)
        assertEquals(60.0, state.user.longitude)
    }

    @Test
    fun `multiple token changes should update state correctly`() = runTest {
        val loginTokens = MutableStateFlow(Tokens("userId", "access", "refresh", 123456789))
        val api = mockk<UserControllerApi>()

        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 10.0
        every { location.longitude } returns 20.0
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository =
            UserRepository(backgroundScope, coroutineContext[CoroutineDispatcher]!!, loginTokens, api)
        runCurrent()

        loginTokens.value = Tokens.Companion.BLANK
        userRepository.userState.first { it is UserState.Disconnected }
        assertEquals(UserState.Disconnected, userRepository.userState.value)

        loginTokens.value = Tokens("user1", "access1", "refresh1", 1111)
        userRepository.userState.first { it is UserState.Connected }
        assertTrue(userRepository.userState.value is UserState.Connected)

        loginTokens.value = Tokens("", "", "", -1)
        userRepository.userState.first { it is UserState.Disconnected }
        assertEquals(UserState.Disconnected, userRepository.userState.value)

        loginTokens.value = Tokens("user2", "access2", "refresh2", 2222)
        userRepository.userState.first { it is UserState.Connected }
        assertTrue(userRepository.userState.value is UserState.Connected)
    }
}