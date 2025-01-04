package com.example.s501.ui.viewmodel

import android.app.Application
import android.util.Log
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

    fun refreshImages(isLocal: Boolean = true) {
        _uiState.value = ImageUiState.Loading
        getAll(isLocal)
    }

    private fun getAll(isLocal: Boolean) {
        viewModelScope.launch {
            try {
                _uiState.value = ImageUiState.Loading

                val result = if (isLocal) {
                    repository.getLocalImages()
                } else {
                    repository.getOnlineImages()
                }

                _uiState.value = ImageUiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = ImageUiState.Error("An error occurred")
                Log.e("ViewModel", e.message ?: "Unknown error")
            }
        }
    }

    suspend fun uploadImage(file: File, categories: List<Category>) {
        withContext(Dispatchers.IO) {
            repository.uploadImage(file, categories)
        }
    }
}