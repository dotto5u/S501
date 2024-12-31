package com.example.s501.data.analysis

import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.s501.data.model.DetectedObject

class DishImageAnalyzer (
    private val detector : DishDetector,
    private val onResult : (List<DetectedObject>) -> Unit
) : ImageAnalysis.Analyzer{

    private var skippedFrames = 0

    override fun analyze(image: ImageProxy) {
        skippedFrames += 1
        if (skippedFrames % 30 != 0){
            image.close()
            return
        }

        val rotationDegrees = image.imageInfo.rotationDegrees
        val bitmap = image
            .toBitmap()

        val imageSize = Size(image.width, image.height)

        val results = detector.detect(bitmap, rotationDegrees, imageSize)
        onResult(results)

        image.close()
    }
}