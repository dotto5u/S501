package com.example.s501.ui.composable.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.s501.R
import com.example.s501.data.model.Image
import com.example.s501.ui.composable.Message
import com.example.s501.ui.theme.subtitleColor
import com.example.s501.ui.viewmodel.ImageViewModel
import com.example.s501.ui.viewmodel.ImageUiState
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HistoryBody(viewModel: ImageViewModel, navController: NavHostController) {
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

                if (images.isEmpty()) {
                    item {
                        Message(
                            message = stringResource(R.string.history_body_no_images),
                            color = Color.DarkGray
                        )
                    }
                } else {
                    items(images) { image ->
                        HistoryImage(image, navController)
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
            is ImageUiState.Error -> {
                item {
                    Message(
                        message = stringResource(R.string.history_body_error_images),
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryImage(image: Image, navController: NavHostController) {
    val imageJson = Gson().toJson(image)
    val encodedImageJson = URLEncoder.encode(imageJson, StandardCharsets.UTF_8.toString())

    val subtitleColor = subtitleColor()

    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            modifier = Modifier
                .height(125.dp)
                .width(150.dp)
                .clickable {
                navController.navigate("image_detail_screen/$encodedImageJson")
                },
            model = image.url,
            contentDescription = null,
            error = painterResource(R.drawable.default_image),
            contentScale = ContentScale.Crop,
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column(modifier = Modifier.padding(vertical = 5.dp)) {
            Text(
                text = stringResource(R.string.history_body_detected_objects),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = subtitleColor
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp, max = 200.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    val categories = image.categories.take(2)

                    categories.forEach { category ->
                        Text(
                            text = category.label.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}