package com.vladislaviliev.meet.ui.loading.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladislaviliev.meet.network.repositories.user.UserRepository
import com.vladislaviliev.meet.ui.loading.LoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class LoadingUserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _state = MutableStateFlow<LoadingState>(LoadingState.Loading)
    val state = _state

    init {
        download()
    }

    fun download() {
        viewModelScope.launch {
            val result = userRepository.download()
            _state.value = when {
                result.isSuccess -> LoadingState.Success
                else -> LoadingState.Error(result.exceptionOrNull()!!.toString())
            }
        }
    }
}