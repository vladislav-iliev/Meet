package com.vladislaviliev.meet.network.repositories.user

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.openapitools.client.apis.UserControllerApi
import org.openapitools.client.models.BaseLocation
import org.openapitools.client.models.UserInfoDto
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryTest {

    private val userId = "userId"
    private val api = mockk<UserControllerApi>()

    @Test
    fun `start with blank user`() = runTest {
        val userRepository = UserRepository(coroutineContext[CoroutineDispatcher]!!, api, userId)
        assertEquals(User.BLANK, userRepository.user.value)
    }

    @Test
    fun `download should set user when API call succeeds`() = runTest {
        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()
        every { location.latitude } returns 15.5
        every { location.longitude } returns 25.5
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository = UserRepository(coroutineContext[CoroutineDispatcher]!!, api, userId)
        userRepository.download()
        runCurrent()

        val user = userRepository.user.value
        assertNotNull(user)
        assertEquals(15.5, user.latitude)
        assertEquals(25.5, user.longitude)
    }

    @Test
    fun `download should return success when API call succeeds`() = runTest {
        val location = mockk<BaseLocation>()
        val userInfoDto = mockk<UserInfoDto>()

        every { location.latitude } returns 15.5
        every { location.longitude } returns 25.5
        every { userInfoDto.location } returns location
        coEvery { api.getUserInfo("userId") } returns userInfoDto

        val userRepository = UserRepository(coroutineContext[CoroutineDispatcher]!!, api, userId)
        val result = userRepository.download()
        runCurrent()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `download should keep user blank when API call fails`() = runTest {
        coEvery { api.getUserInfo("userId") } throws RuntimeException("API Error")

        val userRepository = UserRepository(coroutineContext[CoroutineDispatcher]!!, api, userId)
        userRepository.download()
        runCurrent()
        assertEquals(User.BLANK, userRepository.user.value)
    }

    @Test
    fun `download should return failed result when API call fails`() = runTest {
        val exception = RuntimeException("API Error")
        coEvery { api.getUserInfo("userId") } throws exception

        val userRepository = UserRepository(coroutineContext[CoroutineDispatcher]!!, api, userId)
        val result = userRepository.download()
        runCurrent()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}