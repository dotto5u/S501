package com.example.s501.ui.composable.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.repository.ImageRepository
import com.example.s501.ui.viewmodel.ImageViewModel
import com.example.s501.ui.viewmodel.ImageViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun History() {
    val context = LocalContext.current
    val apiClient = remember { ApiClient() }
    val imageRepository = remember { ImageRepository(apiClient.apiService) }
    val imageViewModel: ImageViewModel = viewModel(factory = ImageViewModelFactory(imageRepository))
    val coroutineScope = rememberCoroutineScope()

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

        Spacer(modifier = Modifier.height(25.dp))
        Button(onClick = {
            coroutineScope.launch {
                imageViewModel.uploadImage(context)
            }
        }) {
            Text("Envoyer")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    History()
}