package com.example.s501

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class DishImageAnalyzer (
    private val classifier : DishClassifier,
    private val onResult : (List<Classification>) -> Unit
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

        val results = classifier.classify(bitmap, rotationDegrees)
        onResult(results)

        image.close()
    }
}