package com.vladislaviliev.meet.network.repositories.login

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class LoginRepositoryProvider {
    private val _current = MutableStateFlow<LoginRepository?>(null)
    val current = _current.asStateFlow()

    fun update(repository: LoginRepository?) {
        _current.value = repository
    }
}