package com.example.s501.ui.viewmodel.history

import com.example.s501.data.model.Image

sealed class HistoryUiState {
    data object Loading: HistoryUiState()
    data class Success(val objects: List<Image>): HistoryUiState()
    data object Error: HistoryUiState()
}
