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
                val analyzer = remember {
                    DishImageAnalyzer(
                        detector = TensorFlowDishDetector(
                            context = applicationContext,
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
                                    //temporary code for debugging

                                    if (detectedObjects != emptyList<DetectedObject>()){
                                        drawContext.canvas.nativeCanvas.drawText(
                                            "Yes",
                                           0f,
                                            0f,
                                            android.graphics.Paint().apply {
                                                color = android.graphics.Color.RED
                                                textSize = 40f
                                            }
                                        )
                                    }
                                    else{
                                        drawContext.canvas.nativeCanvas.drawText(
                                            "No",
                                            0f,
                                            0f,
                                            android.graphics.Paint().apply {
                                                color = android.graphics.Color.RED
                                                textSize = 40f
                                            }
                                        )
                                    }

                                    for (detectedObject in detectedObjects) {
                                        drawRect(
                                            color = androidx.compose.ui.graphics.Color.Green,
                                            topLeft = Offset(
                                                detectedObject.box.left,
                                                detectedObject.box.top
                                            ),
                                            size = Size(
                                                detectedObject.box.width(),
                                                detectedObject.box.height()
                                            ),
                                            style = Stroke(width = 10f),
                                        )

                                        drawContext.canvas.nativeCanvas.drawText(
                                            "${detectedObject.classId}: ${
                                                "%.2f".format(
                                                    detectedObject.certainty * 100
                                                )
                                            }%",
                                            detectedObject.box.left,
                                            detectedObject.box.top - 10,
                                            android.graphics.Paint().apply {
                                                color = android.graphics.Color.RED
                                                textSize = 40f
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
