package com.example.s501.ui.viewmodel.history

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.s501.data.repository.ImageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class HistoryViewModel(
    application: Application,
    private val repository: ImageRepository
): AndroidViewModel(application) {
    private var job: Job? = null
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun fetchImages(isLocal: Boolean = true) {
        job?.cancel()

        job = viewModelScope.launch {
            try {
                _uiState.value = HistoryUiState.Loading

                val result = if (isLocal) {
                    repository.getLocalImages()
                } else {
                    repository.getOnlineImages()
                }

                _uiState.value = HistoryUiState.Success(result)
            } catch (e: CancellationException) {
                Log.d("HistoryViewModel", "Coroutine cancelled")
                throw e
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error
                Log.e("HistoryViewModel", e.message ?: "Unknown error")
            }
        }
    }
}
