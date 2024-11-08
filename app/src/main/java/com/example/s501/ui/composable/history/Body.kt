package com.example.s501.ui.composable.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.s501.R
import com.example.s501.data.model.Image
import com.example.s501.ui.viewmodel.ImageViewModel
import com.example.s501.ui.viewmodel.ImageUiState

@Composable
fun HistoryBody(viewModel: ImageViewModel) {
    val imageUiState by viewModel.uiState.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        when (imageUiState) {
            is ImageUiState.Loading -> {
                item {
                    SkeletonLoading(5)
                }
            }
            is ImageUiState.Success -> {
                val images = (imageUiState as ImageUiState.Success).objects

                items(images) { image ->
                    HistoryImage(image)
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
            is ImageUiState.Error -> {
                val errorMessage = (imageUiState as ImageUiState.Error).message
                item {
                    ErrorMessage(message = errorMessage)
                }
            }
        }
    }
}

// TODO implémenter le chargement des images avec Coil
// TODO adapter / changer la LazyVerticalGrid
// TODO revoir la façon dont la taille des images est gérée
@Composable
fun HistoryImage(image: Image) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(R.drawable.clementine), // image par défault pour tester
            contentDescription = null,
            modifier = Modifier
                .height(125.dp)
                .width(150.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp, max = 200.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(100.dp),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(6.dp)
                ) {
                    items(image.categories) { category ->
                        Text(
                            text = category.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}