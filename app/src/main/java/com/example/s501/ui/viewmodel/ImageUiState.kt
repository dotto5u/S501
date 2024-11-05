package com.example.s501.ui.viewmodel

import com.example.s501.data.model.Image


sealed class ImageUiState {
    data object Loading: ImageUiState()
    data class Success(val objects: List<Image>): ImageUiState()
    data class Error(val message: String): ImageUiState()
}
