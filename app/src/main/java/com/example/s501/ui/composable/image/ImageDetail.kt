package com.example.s501.ui.composable.image

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.s501.R
import com.example.s501.data.json.JsonFileService
import com.example.s501.data.model.Category
import com.example.s501.data.model.Image
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.repository.ImageRepository
import com.example.s501.ui.composable.icons.BackIcon
import com.example.s501.ui.theme.Pink40
import com.example.s501.ui.theme.Purple40
import com.example.s501.ui.theme.subtitleColor
import com.example.s501.ui.viewmodel.image.ButtonUiState
import com.example.s501.ui.viewmodel.image.ImageViewModel
import com.example.s501.ui.viewmodel.image.ImageViewModelFactory
import com.example.s501.ui.viewmodel.user.UserViewModel
import java.io.File

@Composable
fun ImageDetail(
    navController: NavHostController,
    userViewModel: UserViewModel,
    image: Image,
    isLocal: Boolean
) {
    val context = LocalContext.current
    val apiClient = remember { ApiClient() }
    val jsonFileService = remember { JsonFileService(context) }
    val imageRepository = remember { ImageRepository(context, apiClient.apiService, jsonFileService) }
    val imageViewModel: ImageViewModel = viewModel(
        factory = ImageViewModelFactory(
            application = context.applicationContext as Application,
            repository = imageRepository
        )
    )
    val user by userViewModel.user.collectAsState()

    val userId = user?.id ?: -1
    val imageId = image.id.toString()
    val categories = image.categories
    val isUserImage = image.userId == userId

    imageViewModel.fetchImageSyncStatus(imageId)

    val buttonUiState = imageViewModel.buttonUiState.collectAsState().value
    val isSynced = imageViewModel.isImageSynced.collectAsState().value
    val isLoading = buttonUiState is ButtonUiState.Loading
    val enable = if (isLocal) {
        categories.isNotEmpty() && !isLoading
    } else {
        isUserImage && !isLoading
    }

    val syncButtonText = stringResource(R.string.history_image_detail_sync)
    val syncSuccessMessage = stringResource(R.string.history_image_detail_sync_success)
    val unsyncButtonText = stringResource(R.string.history_image_detail_unsync)
    val unsyncSuccessMessage = stringResource(R.string.history_image_detail_unsync_success)
    val buttonText = if (isLocal) {
        if (isSynced) unsyncButtonText else syncButtonText
    } else {
        unsyncButtonText
    }
    val buttonColor = if (isLocal) {
        if (isSynced) Pink40 else Purple40
    } else {
        Pink40
    }
    val subtitleColor = subtitleColor()

    LaunchedEffect(buttonUiState) {
        when (buttonUiState) {
            is ButtonUiState.Success -> {
                val message = if (isSynced) unsyncSuccessMessage else syncSuccessMessage

                imageViewModel.resetButtonUiState()
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (isSynced && !isLocal) navController.popBackStack()
            }
            is ButtonUiState.Error -> {
                val message = context.getString(buttonUiState.resId)

                imageViewModel.resetButtonUiState()
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            ImageDetailBottomBar(
                onClick = {
                    if (isLocal) {
                        if (isSynced) {
                            imageViewModel.deleteImage(imageId, userId)
                        } else {
                            imageViewModel.uploadImage(image, userId, categories)
                        }
                    } else {
                        if (isUserImage) {
                            imageViewModel.deleteImage(imageId, userId)
                        }
                    }
                },
                text = buttonText,
                color = buttonColor,
                enable = enable,
                isLoading = isLoading
            )
        },
        topBar = {
            ImageDetailTopBar(context, navController, image, isLocal)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.7f)
                ) {
                    AsyncImage(
                        modifier = Modifier.fillMaxSize(),
                        model = image.url,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        error = painterResource(R.drawable.default_image)
                    )
                }
                Spacer(modifier = Modifier.height(15.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                ) {
                    ImageDetailObjectList(categories, subtitleColor)
                }
            }
        }
    }
}

@Composable
fun ImageDetailTopBar(
    context: Context,
    navController: NavHostController,
    image: Image,
    isLocal: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, start = 5.dp, end = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackIcon(navController)
        if (isLocal) {
            Button(
                modifier = Modifier.padding(end = 5.dp, top = 15.dp),
                onClick = {
                    val file = File(image.url.substringAfter("file://"))

                    if (file.exists() && file.delete()) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.history_image_detail_delete_success),
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.popBackStack()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.history_image_detail_delete_fail),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ImageDetailObjectList(categories: List<Category>, subtitleColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (categories.isEmpty()) {
            Text(
                text = stringResource(R.string.history_body_no_detected_objects),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = subtitleColor
            )
        } else {
            Text(
                text =  stringResource(R.string.history_body_detected_objects),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = subtitleColor
            )
            Spacer(modifier = Modifier.height(15.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                categories.forEach { category ->
                    ImageDetailObject(category)
                    Spacer(modifier = Modifier.width(15.dp))
                }
            }
        }
    }
}

@Composable
fun ImageDetailObject(category: Category) {
    Box(
        modifier = Modifier
            .background(
                shape = RoundedCornerShape(16.dp),
                color = Color.LightGray
            )
            .padding(horizontal = 15.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.label.replaceFirstChar { it.uppercase() },
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        )
    }
}

@Composable
fun ImageDetailBottomBar(
    onClick: () -> Unit,
    text: String,
    color: Color,
    enable: Boolean = true,
    isLoading: Boolean = false
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                onClick = onClick,
                enabled = enable,
                colors = ButtonDefaults.buttonColors(
                    containerColor = color,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text)
                }
            }
        }
    }
}