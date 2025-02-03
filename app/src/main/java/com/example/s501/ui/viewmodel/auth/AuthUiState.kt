package com.example.s501.ui.viewmodel.auth

import com.example.s501.data.model.User

sealed class AuthUiState {
    data object Idle: AuthUiState()
    data object Loading: AuthUiState()
    data class Success(val user: User): AuthUiState()
    data object Error: AuthUiState()
}
