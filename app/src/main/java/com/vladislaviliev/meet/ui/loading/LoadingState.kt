package com.vladislaviliev.meet.ui.loading

internal sealed class LoadingState {
    object Success : LoadingState()
    object Loading : LoadingState()
    data class Error(val message: String) : LoadingState()
}