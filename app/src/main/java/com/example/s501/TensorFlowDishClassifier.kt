package com.example.s501

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions

class TensorFlowDishClassifier(
    private val context : Context,
    private val precisionThreshold : Float = 0.4f,
    private val maxResults : Int = 1
) : DishClassifier {

    private val modelPath = "2.tflite"

    private var classifier : ObjectDetector? = null

    private fun setupClassifier(){
        val baseOptions : BaseOptions = BaseOptions.builder()
            .setNumThreads(2)
            .build()

        val options = ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(maxResults)
            .setScoreThreshold(precisionThreshold)
            .build()

        try{
            classifier = ObjectDetector.createFromFileAndOptions(
                context,
                modelPath,
                options
            )
        }
        catch (e : IllegalStateException){
            e.printStackTrace()
        }
    }

    override fun classify(bitmap: Bitmap, rotation: Int): List<Classification> {
        if (classifier == null){
            setupClassifier()
        }

        val imageProcessor = ImageProcessor.Builder().build()

        val tensorFlowImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()

        val results = classifier?.detect(tensorFlowImage, imageProcessingOptions)

        val tempResults = mutableListOf<Classification>();

        results?.forEach {
            tempResults.add(
                Classification(
                    name = it.categories.maxOf { it.label },
                    certainty = it.categories.maxOf { it.score },
                    box = it.boundingBox
                )
            )
        }

        Log.w("tempResults", tempResults.toString())

        return tempResults;
    }

    private fun getOrientationFromRotation(rotation : Int) : ImageProcessingOptions.Orientation{
        return when(rotation){
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }
}