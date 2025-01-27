package com.example.s501.ui.viewmodel.image

sealed class ButtonUiState {
    data object Idle: ButtonUiState()
    data object Loading: ButtonUiState()
    data object Success: ButtonUiState()
    data object Error: ButtonUiState()
}