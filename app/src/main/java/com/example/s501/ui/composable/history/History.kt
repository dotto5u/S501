package com.example.s501.ui.composable.history

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.s501.data.json.JsonFileService
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.repository.ImageRepository
import com.example.s501.ui.composable.BottomNavbar
import com.example.s501.ui.viewmodel.history.HistoryViewModel
import com.example.s501.ui.viewmodel.history.HistoryViewModelFactory

@Composable
fun History(navController: NavHostController) {
    val context = LocalContext.current
    val apiClient = remember { ApiClient() }
    val jsonFileService = remember { JsonFileService(context) }
    val imageRepository = remember { ImageRepository(context, apiClient.apiService, jsonFileService) }
    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(
            application = context.applicationContext as Application,
            repository = imageRepository
        )
    )

    val isLocal = remember { mutableStateOf(true) }

    LaunchedEffect(isLocal.value) {
        historyViewModel.fetchImages(isLocal.value)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavbar(
                currentScreen = "History",
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            HistoryHeader(selectedValue = isLocal)
            Spacer(modifier = Modifier.height(15.dp))
            HistoryBody(
                isLocal = isLocal.value,
                viewModel = historyViewModel,
                navController = navController
            )
        }
    }
}
