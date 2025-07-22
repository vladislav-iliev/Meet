package com.vladislaviliev.meet.ui.loading.session

import androidx.lifecycle.ViewModel
import com.vladislaviliev.meet.session.SessionRepository
import com.vladislaviliev.meet.ui.loading.LoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class SessionViewModel(sessionRepository: SessionRepository) : ViewModel() {

    private val _state = MutableStateFlow<LoadingState>(LoadingState.Loading)
    val state = _state.asStateFlow()

    init {
        sessionRepository.restartSession()
        _state.value = LoadingState.Success
    }
}