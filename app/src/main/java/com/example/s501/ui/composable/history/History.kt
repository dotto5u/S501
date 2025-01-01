package com.example.s501.ui.composable.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.repository.ImageRepository
import com.example.s501.ui.viewmodel.ImageViewModel
import com.example.s501.ui.viewmodel.ImageViewModelFactory

@Composable
fun History() {
    val apiClient = remember { ApiClient() }
    val imageRepository = remember { ImageRepository(apiClient.apiService) }
    val imageViewModel: ImageViewModel = viewModel(factory = ImageViewModelFactory(imageRepository))

    DisposableEffect(Unit) {
        imageViewModel.refreshImages()
        onDispose {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        HistoryHeader()
        Spacer(modifier = Modifier.height(25.dp))
        HistoryBody(viewModel = imageViewModel)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    History()
}