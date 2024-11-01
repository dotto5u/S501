package com.example.s501

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class DishImageAnalyzer (
    private val detector : DishDetector,
    private val onResult : (List<DetectedObject>) -> Unit
) : ImageAnalysis.Analyzer{

    private var skippedFrames = 0;

    override fun analyze(image: ImageProxy) {
        if (skippedFrames % 30 == 0){
            val rotationDegrees = image.imageInfo.rotationDegrees;
            val bitmap = image.toBitmap();

            val results = detector.detect(bitmap, rotationDegrees);
            onResult(results);
        }
        skippedFrames += 1;
        image.close();
    }
}