package com.vladislaviliev.meet.ui.login

internal sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Error(val message: String) : LoginState()
    object Success : LoginState()
}