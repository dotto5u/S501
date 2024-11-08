package com.example.s501

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.WindowManager
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.s501.ui.screen.History
import com.example.s501.ui.theme.S501Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        }
        enableEdgeToEdge()
        setContent {
            S501Theme {
                var currentScreen by remember { mutableStateOf("Camera") }
                var detectedObjects by remember {
                    mutableStateOf(emptyList<DetectedObject>())
                }
                val displayMetrics = DisplayMetrics()
                val windowManager = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                windowManager.defaultDisplay.getMetrics(displayMetrics);
                val analyzer = remember {
                    DishImageAnalyzer(
                        detector = TensorFlowDishDetector(
                            context = applicationContext,
                            screenWidth = displayMetrics.widthPixels,
                            screenHeight = displayMetrics.heightPixels,
                        ),
                        onResult = {
                            detectedObjects = it
                        }
                    )
                }
                val controller = remember {
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            CameraController.IMAGE_ANALYSIS or
                                    CameraController.IMAGE_CAPTURE
                        )
                        setImageAnalysisAnalyzer(
                            ContextCompat.getMainExecutor(applicationContext),
                            analyzer
                        )
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        MyBottomNavbar { screen ->
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
                                        .align(Alignment.TopCenter))


                                Canvas(
                                    modifier = Modifier.fillMaxSize(),

                                    ) {

                                    for (detectedObject in detectedObjects) {
                                        drawRect(
                                            color = androidx.compose.ui.graphics.Color.Green,
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
}
