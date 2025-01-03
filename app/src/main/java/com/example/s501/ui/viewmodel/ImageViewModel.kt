package com.example.s501.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.s501.data.model.Category
import com.example.s501.data.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageViewModel(
    application: Application,
    private val repository: ImageRepository
): AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<ImageUiState>(ImageUiState.Loading)
    val uiState: StateFlow<ImageUiState> = _uiState.asStateFlow()

    fun refreshImages(isLocal: Boolean = false) {
        getAll(isLocal)
    }
    private fun getAll(isLocal: Boolean) {
        viewModelScope.launch {
            try {
                _uiState.value =
                if (isLocal) {
                    ImageUiState.Success(repository.getLocalImages())
                } else {
                    ImageUiState.Success(repository.getOnlineImages())
                }
            } catch (e: Exception) {
                _uiState.value = ImageUiState.Error("An error occurred")
                println("ViewModelError: ${e.message ?: "Unknown"}")
            }
        }
    }

    suspend fun uploadImage(file: File, categories: List<Category>) {
        withContext(Dispatchers.IO) {
            repository.uploadImage(file, categories)
        }
    }
}