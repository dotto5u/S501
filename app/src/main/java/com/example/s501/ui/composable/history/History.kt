package com.example.s501.ui.composable.history

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.s501.data.json.JsonFileService
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.repository.ImageRepository
import com.example.s501.ui.composable.BottomNavbar
import com.example.s501.ui.viewmodel.ImageViewModel
import com.example.s501.ui.viewmodel.ImageViewModelFactory

@Composable
fun History(currentScreen: String, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val apiClient = remember { ApiClient() }
    val jsonFileService = remember { JsonFileService(context) }
    val imageRepository = remember { ImageRepository(context, apiClient.apiService, jsonFileService) }
    val imageViewModel: ImageViewModel = viewModel(factory = ImageViewModelFactory(context.applicationContext as Application, imageRepository))

    DisposableEffect(Unit) {
        imageViewModel.refreshImages(false)
        onDispose {}
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavbar(
                currentScreen = currentScreen,
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            HistoryHeader()
            Spacer(modifier = Modifier.height(25.dp))
            HistoryBody(viewModel = imageViewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    History(currentScreen = "History", onNavigate = { println("Camera") })
}