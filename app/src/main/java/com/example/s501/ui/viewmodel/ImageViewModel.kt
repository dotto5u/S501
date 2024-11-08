package com.example.s501.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s501.data.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageViewModel(private val repository: ImageRepository): ViewModel() {
    private val _uiState = MutableStateFlow<ImageUiState>(ImageUiState.Loading)
    val uiState: StateFlow<ImageUiState> = _uiState.asStateFlow()

    fun refreshImages() {
        getAll()
    }

    private fun getAll() {
        viewModelScope.launch {
            try {
                _uiState.value = ImageUiState.Success(repository.getAll())
            } catch (e: Exception) {
                _uiState.value = ImageUiState.Error("An error occurred")
                println("ViewModelError: ${e.message ?: "Unknown"}")
            }
        }
    }
}