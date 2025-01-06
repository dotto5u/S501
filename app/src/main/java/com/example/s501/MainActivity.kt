package com.example.s501

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.s501.ui.composable.history.History
import com.example.s501.ui.theme.S501Theme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.s501.data.analysis.DishImageAnalyzer
import com.example.s501.data.analysis.TensorFlowDishDetector
import com.example.s501.data.json.JsonFileService
import com.example.s501.data.model.Category
import com.example.s501.data.model.DetectedObject
import com.example.s501.data.model.Image
import com.example.s501.ui.composable.BottomNavbar
import com.example.s501.ui.composable.camera.CameraPreview
import com.example.s501.ui.composable.image.ImageDetail
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var jsonFileService: JsonFileService
    private var detectedObjects by mutableStateOf(emptyList<DetectedObject>())
    private var cameraPreviewSize = mutableStateOf(Size(0f, 0f))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }

        jsonFileService = JsonFileService(applicationContext)
        jsonFileService.createFileIfNotExists()

        enableEdgeToEdge()

        setContent {
            S501Theme {
                val analyzer = remember {
                    mutableStateOf<DishImageAnalyzer?>(null)
                }
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_ANALYSIS or
                                    CameraController.IMAGE_CAPTURE
                        )
                    }
                }

                LaunchedEffect(cameraPreviewSize) {
                    if (cameraPreviewSize.value.width > 0 && cameraPreviewSize.value.height > 0) {
                        analyzer.value = DishImageAnalyzer(
                            detector = TensorFlowDishDetector(
                                context = applicationContext,
                                screenWidth = cameraPreviewSize.value.width,
                                screenHeight = cameraPreviewSize.value.height,
                            ),
                            onResult = { detectedObjects = it }
                        )
                        controller.setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext),
                            analyzer.value!!
                        )
                    }
                }

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "camera_screen",
                ) {
                    composable(
                        "image_detail_screen/{image}/{isLocal}",
                        arguments = listOf(
                            navArgument("image") { type = NavType.StringType },
                            navArgument("isLocal") { type = NavType.BoolType }
                        )
                    ) { backStackEntry ->
                        val encodedImageJson = backStackEntry.arguments?.getString("image")
                        val imageJson = URLDecoder.decode(encodedImageJson, StandardCharsets.UTF_8.toString())
                        val image = Gson().fromJson(imageJson, Image::class.java)

                        val isLocal = backStackEntry.arguments?.getBoolean("isLocal") ?: true

                        image?.let {
                            ImageDetail(image = it, isLocal = isLocal, onNavigateBack = { navController.popBackStack() })
                        }
                    }
                    composable("history_screen") {
                        History(navController)
                    }
                    composable("camera_screen") {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                BottomNavbar(
                                    currentScreen = "Camera",
                                    navController = navController
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                CameraPreview(controller,
                                    Modifier
                                        .fillMaxSize()
                                        .align(Alignment.TopCenter)
                                        .onSizeChanged { size->
                                            cameraPreviewSize.value = Size(
                                                size.width.toFloat(),
                                                size.height.toFloat()
                                            )
                                        }
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    for (detectedObject in detectedObjects) {
                                        drawRect(
                                            color = Color.Green,
                                            topLeft = Offset(
                                                detectedObject.box.left,
                                                detectedObject.box.top,
                                            ),
                                            size = Size(
                                                detectedObject.box.width(),
                                                detectedObject.box.height()
                                            ),
                                            style = Stroke(width = 20f),
                                        )

                                        drawContext.canvas.nativeCanvas.drawText(
                                            "${detectedObject.name}: ${
                                                "%.2f".format(
                                                    detectedObject.certainty * 100
                                                )
                                            }%",
                                            detectedObject.box.left,
                                            detectedObject.box.top - 10,
                                            Paint().apply {
                                                color = android.graphics.Color.RED
                                                textSize = 80f
                                            }
                                        )

                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(Color.Transparent, shape = CircleShape)
                                            .border(5.dp, Color.White, CircleShape)
                                            .align(Alignment.Center)
                                    ) {
                                        Button(
                                            onClick = {
                                                capturePhotoWithOverlay(controller)
                                            },
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent
                                            ),
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun createOverlayBitmap(photoWidth: Int, photoHeight: Int): Bitmap {
        val overlayBitmap = Bitmap.createBitmap(photoWidth, photoHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(overlayBitmap)

        val scaleX = photoWidth.toFloat() / cameraPreviewSize.value.width
        val scaleY = photoHeight.toFloat() / cameraPreviewSize.value.height

        for (detectedObject in detectedObjects) {
            val rectPaint = Paint().apply {
                color = android.graphics.Color.GREEN
                strokeWidth = 5f
                style = Paint.Style.STROKE
            }
            val textPaint = Paint().apply {
                color = android.graphics.Color.RED
                textSize = 40f
            }

            // Ajustez les coordonnées des rectangles en fonction des facteurs d'échelle
            val scaledLeft = detectedObject.box.left * scaleX
            val scaledTop = detectedObject.box.top * scaleY
            val scaledRight = detectedObject.box.right * scaleX
            val scaledBottom = detectedObject.box.bottom * scaleY

            // Dessinez le rectangle ajusté
            canvas.drawRect(scaledLeft, scaledTop, scaledRight, scaledBottom, rectPaint)

            // Dessinez le texte au-dessus du rectangle
            canvas.drawText(
                "${detectedObject.name}: ${"%.2f".format(detectedObject.certainty * 100)}%",
                scaledLeft,
                scaledTop - 10,
                textPaint
            )
        }

        return overlayBitmap
    }

    private fun combineBitmaps(capturedBitmap: Bitmap, overlayBitmap: Bitmap): Bitmap {
        // Créer un nouveau Bitmap avec la même taille que l'image capturée
        val combinedBitmap = Bitmap.createBitmap(capturedBitmap.width, capturedBitmap.height, capturedBitmap.config)

        // Dessiner l'image capturée et l'overlay
        val canvas = android.graphics.Canvas(combinedBitmap)
        canvas.drawBitmap(capturedBitmap, 0f, 0f, null)
        canvas.drawBitmap(overlayBitmap, 0f, 0f, null)

        return combinedBitmap
    }

    private fun capturePhotoWithOverlay(controller: LifecycleCameraController) {
        val fileName = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/S501")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { outputUri ->
            val imageId = getImageIdFromUri(outputUri)

            val categories = detectedObjects.mapIndexed { index, detectedObject ->
                Category(id = index, label = detectedObject.name)
            }

            jsonFileService.addCategoriesToJsonFile(imageId, categories)

            controller.takePicture(
                ImageCapture.OutputFileOptions.Builder(contentResolver.openOutputStream(outputUri)!!).build(),
                ContextCompat.getMainExecutor(applicationContext),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val capturedImageUri = output.savedUri ?: outputUri
                        val capturedBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(capturedImageUri))

                        // Validate capturedBitmap
                        if (capturedBitmap == null) {
                            Log.e("Capture", "Failed to load captured bitmap")
                            Toast.makeText(applicationContext, "Erreur lors de la capture de l'image", Toast.LENGTH_SHORT).show()
                            return
                        }

                        // Create the overlay bitmap with correct dimensions
                        val overlayBitmap = createOverlayBitmap(capturedBitmap.width, capturedBitmap.height)

                        // Combine the captured photo with the overlay
                        val combinedBitmap = combineBitmaps(capturedBitmap, overlayBitmap)

                        saveCombinedImage(combinedBitmap, capturedImageUri)

                        Toast.makeText(applicationContext, "Photo enregistrée avec overlay!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(applicationContext, "Erreur lors de l'enregistrement!", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun saveCombinedImage(combinedBitmap: Bitmap, uri: Uri) {
        val outputStream = contentResolver.openOutputStream(uri)
        if (outputStream != null) {
            combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            Log.d("Save", "Image saved to $uri")
        } else {
            // Handle the error when the outputStream is null
            Toast.makeText(applicationContext, "Erreur lors de l'enregistrement de l'image!", Toast.LENGTH_SHORT).show()
            Log.e("Save", "Failed to obtain output stream for URI: $uri")
        }
    }

    private fun getImageIdFromUri(uri: Uri): Long {
        var imageId: Long = -1
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                imageId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            }
        }

        return imageId
    }
}
