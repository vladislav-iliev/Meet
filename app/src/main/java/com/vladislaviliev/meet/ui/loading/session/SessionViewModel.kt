package com.vladislaviliev.meet.ui.loading.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vladislaviliev.meet.session.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.stateIn

internal class SessionViewModel(sessionRepository: SessionRepository) : ViewModel() {

    val isSessionActive = sessionRepository.isSessionActive
        .drop(1)
        .dropWhile { !it }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        sessionRepository.restartSession()
    }
}