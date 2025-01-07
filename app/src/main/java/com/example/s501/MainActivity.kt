package com.example.s501

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.s501.data.analysis.DishImageAnalyzer
import com.example.s501.data.analysis.TensorFlowDishDetector
import com.example.s501.data.model.Category
import com.example.s501.data.model.DetectedObject
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.remote.ApiService
import com.example.s501.data.repository.ImageRepository
import com.example.s501.ui.composable.BottomNavbar
import com.example.s501.ui.composable.camera.CameraPreview
import com.example.s501.ui.composable.history.History
import com.example.s501.ui.theme.S501Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var detectedObjects by mutableStateOf(emptyList<DetectedObject>())
    private var cameraPreviewSize = mutableStateOf(Size(0f, 0f))
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val selectedImage = BitmapFactory.decodeStream(inputStream)
                if (selectedImage != null) {
                    Log.d("Uri selected image",it.toString())
                    analyzeImageFromGallery(it)
                } else {
                    Toast.makeText(applicationContext, "Erreur lors du chargement de l'image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Erreur lors de l'ouverture de l'image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
        enableEdgeToEdge()
        setContent {
            S501Theme {
                var currentScreen by remember { mutableStateOf("Camera") }

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

                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomNavbar { screen ->
                            currentScreen = screen
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            "History" -> History()
                            "Camera" -> Box(
                                modifier = Modifier.fillMaxSize()
                            )
                            {
                                CameraPreview(controller,
                                    Modifier
                                        .fillMaxSize()
                                        .align(Alignment.TopCenter)
                                        .onSizeChanged { size ->
                                            cameraPreviewSize.value = Size(
                                                size.width.toFloat(),
                                                size.height.toFloat()
                                            )
                                        }
                                )


                                Canvas(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
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
                                            android.graphics.Paint().apply {
                                                color = android.graphics.Color.GREEN
                                                textSize = 80f
                                            }
                                        )

                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            openGallery()                                        },
                                        modifier = Modifier.size(80.dp),
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddCircle,
                                            contentDescription = "Upload Icon",
                                            tint = Color.White,
                                            modifier = Modifier.size(100.dp)
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
                                            )
                                        ) {
                                            Text(
                                                text = "",
                                                color = Color.White,
                                                fontSize = 24.sp
                                            )
                                        }
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

    private fun createOverlayBitmap(bitmap: Bitmap, detectedObjects: List<DetectedObject>): Bitmap {
        val overlayBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(overlayBitmap)

        val scaleX = bitmap.width.toFloat() / cameraPreviewSize.value.width
        val scaleY = bitmap.height.toFloat() / cameraPreviewSize.value.height

        // Draw bounding boxes for each detected object
        for (detectedObject in detectedObjects) {
            val rectPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GREEN
                strokeWidth = 5f
                style = android.graphics.Paint.Style.STROKE
            }
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.RED
                textSize = 40f
            }
            val scaledLeft = detectedObject.box.left * scaleX
            val scaledTop = detectedObject.box.top * scaleY
            val scaledRight = detectedObject.box.right * scaleX
            val scaledBottom = detectedObject.box.bottom * scaleY
            canvas.drawRect(scaledLeft, scaledTop, scaledRight, scaledBottom, rectPaint)
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
        val combinedBitmap = Bitmap.createBitmap(capturedBitmap.width, capturedBitmap.height, capturedBitmap.config)

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
            controller.takePicture(
                ImageCapture.OutputFileOptions.Builder(contentResolver.openOutputStream(outputUri)!!).build(),
                ContextCompat.getMainExecutor(applicationContext),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val capturedImageUri = output.savedUri ?: outputUri
                        val capturedBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(capturedImageUri))

                        if (capturedBitmap == null) {
                            Log.e("Capture", "Failed to load captured bitmap")
                            Toast.makeText(applicationContext, "Erreur lors de la capture de l'image", Toast.LENGTH_SHORT).show()
                            return
                        }
                        val overlayBitmap = createOverlayBitmap(capturedBitmap, detectedObjects)
                        val combinedBitmap = combineBitmaps(capturedBitmap, overlayBitmap)

                        //Saving image
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
            combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.close()
            Log.d("Save", "Image saved to $uri")
        } else {
            Toast.makeText(applicationContext, "Erreur lors de l'enregistrement de l'image!", Toast.LENGTH_SHORT).show()
            Log.e("Save", "Failed to obtain output stream for URI: $uri")
        }
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun analyzeImageFromGallery(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.use { stream ->
            val bitmap = BitmapFactory.decodeStream(stream)
            if (bitmap != null) {

                val overlayBitmap = createOverlayBitmap(bitmap, detectedObjects)
                val combinedBitmap = combineBitmaps(bitmap, overlayBitmap)
                Log.d("Detected Object : ", detectedObjects.toString())

                saveCombinedImageToGallery(combinedBitmap)
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(this, "Failed to open image", Toast.LENGTH_SHORT).show()
    }

    private fun saveCombinedImageToGallery(combinedBitmap: Bitmap) {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/S501")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { outputUri ->
            val outputStream = contentResolver.openOutputStream(outputUri)
            outputStream?.use {
                combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, it)
                it.close()
                Toast.makeText(applicationContext, "Image saved with overlay", Toast.LENGTH_SHORT).show()
            }
        } ?: Toast.makeText(applicationContext, "Failed to save image", Toast.LENGTH_SHORT).show()
    }
}
