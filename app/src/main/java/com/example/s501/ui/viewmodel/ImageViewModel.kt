package com.example.s501.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s501.R
import com.example.s501.data.model.Category
import com.example.s501.data.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    suspend fun uploadImage(context: Context) {
        // TODO remplacer les données de test
        val stream = context.resources.openRawResource(R.raw.clementine)
        val file = File(context.cacheDir, "image.jpg")
        withContext(Dispatchers.IO) {
            stream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        val categories = listOf(
            Category(id = -1, label = "Végétarien"),
            Category(id = -1, "Italien")
        )

        withContext(Dispatchers.IO) {
            repository.uploadImage(file, categories)
        }
    }
}