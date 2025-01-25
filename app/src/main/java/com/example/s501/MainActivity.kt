package com.example.s501

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.s501.ui.composable.BottomNavbar
import com.example.s501.ui.composable.camera.CameraPreview
import com.example.s501.ui.composable.history.History
import com.example.s501.ui.theme.S501Theme
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.sharp.Person
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

    private val imageChooseCode = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasCameraPermission()) {
            askForCameraPermission()
        }
        if (!hasStorageReadPermission()){
            askForStorageReadPermission()
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
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None },
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
                            },
                            topBar = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Sharp.Person, // Replace with the desired icon
                                        contentDescription = "User Icon",
                                        tint = Color.Magenta,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd) // Align the icon to the top right
                                            .size(40.dp) // Set the size of the icon
                                            .clickable {
                                                val intent = Intent(applicationContext, LoginActivity::class.java)
                                                startActivity(intent)
                                            }
                                    )
                                }
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
                                        .onSizeChanged { size ->
                                            cameraPreviewSize.value = Size(
                                                size.width.toFloat(),
                                                size.height.toFloat()
                                            )
                                        }
                                )

                                ChooseFromGalleryButton()

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

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    private fun hasStorageReadPermission() : Boolean{
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun createOverlayBitmap(photoWidth: Int, photoHeight: Int): Bitmap {
        val overlayBitmap = Bitmap.createBitmap(photoWidth, photoHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(overlayBitmap)

        val scaleX = photoWidth.toFloat() / cameraPreviewSize.value.width
        val scaleY = photoHeight.toFloat() / cameraPreviewSize.value.height

        for (detectedObject in detectedObjects) {
            val rectPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GREEN
                strokeWidth = 20f
                style = android.graphics.Paint.Style.STROKE
            }
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.GREEN
                textSize = 80f
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

        val canvas = Canvas(combinedBitmap)

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
                        Log.d("ShowUri", capturedImageUri.toString())
                        val capturedBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(capturedImageUri))

                        if (capturedBitmap == null) {
                            Log.e("Capture", "Failed to load captured bitmap")
                            Toast.makeText(applicationContext, "Erreur lors de la capture de l'image", Toast.LENGTH_SHORT).show()
                            return
                        }
                        val rotatedBitmap = rotateBitmap(capturedBitmap, 90)

                        val overlayBitmap = createOverlayBitmap(rotatedBitmap.width, rotatedBitmap.height)
                        val combinedBitmap = combineBitmaps(rotatedBitmap, overlayBitmap)


                        //Saving image
                        saveCapturedImage(combinedBitmap, capturedImageUri)
                        Toast.makeText(applicationContext, "Photo enregistrée avec overlay!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(applicationContext, "Erreur lors de l'enregistrement de l'image", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    private fun saveCapturedImage(combinedBitmap: Bitmap, uri: Uri) {
        val outputStream = contentResolver.openOutputStream(uri)
        if (outputStream != null) {
            combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            outputStream.close()
            Log.d("Save", "Image saved to $uri")
        } else {
            Toast.makeText(applicationContext, "Erreur lors de l'enregistrement de l'image", Toast.LENGTH_SHORT).show()
            Log.e("Save", "Failed to obtain output stream for URI: $uri")
        }
    }

    private fun askForStorageReadPermission(){
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1)

    }

    private fun askForCameraPermission(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA
            ),
            0)
    }

    @Composable
    fun ChooseFromGalleryButton() {
        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = { chooseImageInGallery() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(25.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Upload Icon",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }

    private fun chooseImageInGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, imageChooseCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == imageChooseCode && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data

            imageUri?.let {
                val bitmap = getBitmapFromUri(it)

                val detector = TensorFlowDishDetector(
                    context = applicationContext,
                    bitmap!!.width.toFloat(),
                    bitmap.height.toFloat()
                )
                val bitmapDetections = detector.detect(
                    bitmap,
                    0,
                    android.util.Size(bitmap.width, bitmap.height))

                val bitmapCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(bitmapCopy)

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GREEN
                    strokeWidth = 20F
                    style = android.graphics.Paint.Style.STROKE
                }

                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GREEN
                    textSize = 80f
                    style = android.graphics.Paint.Style.FILL
                }


                for (detection in bitmapDetections){
                    canvas.drawRect(detection.box, paint)

                    val textX = detection.box.left
                    val textY = detection.box.top - 10
                    canvas.drawText("\"${detection.name}: ${
                        "%.2f".format(
                            detection.certainty * 100
                        )
                    }%\"", textX, textY, textPaint)
                }


                saveImportedImage(bitmapCopy, bitmapDetections)
            }
        }
    }

    private fun saveImportedImage(combinedBitmap: Bitmap, detectedObjects: List<DetectedObject>) {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/S501")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        uri?.let { outputUri ->
            //Sauvegarde des catégories en local
            val imageId = getImageIdFromUri(outputUri)
            val categories = detectedObjects.mapIndexed { index, detectedObject ->
                Category(id = index, label = detectedObject.name)
            }
            jsonFileService.addCategoriesToJsonFile(imageId, categories)

            val outputStream = contentResolver.openOutputStream(outputUri)
            outputStream?.use {
                combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, it)
                it.close()
                Toast.makeText(applicationContext, "Image analysée et sauvegardée avec succès !", Toast.LENGTH_SHORT)
                    .show()
            }
        } ?: Toast.makeText(applicationContext, "Impossible de sauvegarder l'image analysée", Toast.LENGTH_SHORT).show()
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
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
