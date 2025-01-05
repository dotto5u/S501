package com.example.s501.ui.composable.image

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import com.example.s501.data.model.Image

// TODO terminer la page
@Composable
fun ImageDetail(image: Image, onNavigateBack: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(paddingValues)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back"
            )
        }
    }
}

