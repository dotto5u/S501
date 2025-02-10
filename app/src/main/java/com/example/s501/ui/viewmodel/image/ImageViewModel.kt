package com.example.s501.ui.viewmodel.image

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.s501.R
import com.example.s501.data.model.Category
import com.example.s501.data.model.Image
import com.example.s501.data.model.ImageCategory
import com.example.s501.data.repository.ImageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ImageViewModel(
    application: Application,
    private val repository: ImageRepository
): AndroidViewModel(application) {
    private var job: Job? = null
    private val _buttonUiState = MutableStateFlow<ButtonUiState>(ButtonUiState.Idle)
    val buttonUiState: StateFlow<ButtonUiState> = _buttonUiState.asStateFlow()
    private val _isImageSynced = MutableStateFlow(false)
    val isImageSynced: StateFlow<Boolean> = _isImageSynced

    fun resetButtonUiState() {
        _buttonUiState.value = ButtonUiState.Idle
    }

    fun fetchImageSyncStatus(imageId: String) {
        viewModelScope.launch {
            try {
                val image = repository.getOnlineImage(imageId)

                _isImageSynced.value = (image != null)
            } catch (e: Exception) {
                _isImageSynced.value = false
                Log.e("ImageViewModel", e.message ?: "Error while checking sync status")
            }
        }
    }

    fun uploadImage(image: Image, userId: Int, categories: List<Category>) {
        job?.cancel()

        job = viewModelScope.launch {
            try {
                if (userId == -1) {
                    _buttonUiState.value = ButtonUiState.Error(R.string.history_image_detail_not_connected_upload)
                } else if (categories.isEmpty()) {
                    _buttonUiState.value = ButtonUiState.Error(R.string.history_image_detail_no_categories)
                } else {
                    _buttonUiState.value = ButtonUiState.Loading

                    val file = File(image.url.substringAfter("file://"))
                    val imageCategory = ImageCategory(
                        imageId = image.id,
                        userId = userId,
                        categories = categories
                    )

                    val success = repository.uploadImage(file, imageCategory)

                    if (success) {
                        _buttonUiState.value = ButtonUiState.Success
                    } else {
                        _buttonUiState.value = ButtonUiState.Error(R.string.history_image_detail_sync_fail)
                    }
                }
            } catch (e: Exception) {
                _buttonUiState.value = ButtonUiState.Error(R.string.history_image_detail_sync_fail)
                Log.e("ImageViewModel", e.message ?: "Unknown error")
            }
        }
    }

    fun deleteImage(imageId: String, userId: Int) {
        job?.cancel()

        job = viewModelScope.launch {
            try {
                if (userId == -1) {
                    _buttonUiState.value = ButtonUiState.Error(R.string.history_image_detail_not_connected_delete)
                } else {
                    _buttonUiState.value = ButtonUiState.Loading

                    val success = repository.deleteImage(imageId)

                    _buttonUiState.value = if (success) {
                        ButtonUiState.Success
                    } else {
                        ButtonUiState.Error(R.string.history_image_detail_unsync_fail)
                    }
                }
            } catch (e: Exception) {
                _buttonUiState.value = ButtonUiState.Error(R.string.history_image_detail_unsync_fail)
                Log.e("ImageViewModel", e.message ?: "Unknown error")
            }
        }
    }

}
