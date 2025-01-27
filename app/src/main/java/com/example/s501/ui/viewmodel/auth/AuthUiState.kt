package com.example.s501.ui.viewmodel.auth

sealed class AuthUiState {
    data object Idle: AuthUiState()
    data object Loading: AuthUiState()
    data object Success: AuthUiState()
    data object Error: AuthUiState()
}
