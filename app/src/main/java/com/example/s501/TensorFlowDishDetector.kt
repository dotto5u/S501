package com.example.s501

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import android.view.Surface
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions
import java.util.Arrays

class TensorFlowDishDetector(
    private val context : Context,
    private val screenWidth : Int,
    private val screenHeight : Int,
) : DishDetector {

    private val modelWantedWidth = 300;
    private val modelWantedHeight = 300;

    private val maxResults : Int = 1;

    private val precisionThreshold : Float = 0.4f;

    private val modelPath = "SDDMobilenetV1.tflite"

    private var detector : ObjectDetector? = null

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
            detector = ObjectDetector.createFromFileAndOptions(
                context,
                modelPath,
                options
            )
        }
        catch (e : IllegalStateException){
            e.printStackTrace()
        }
    }

    override fun detect(bitmap: Bitmap, rotation: Int): List<DetectedObject> {
        if (detector == null){
            setupClassifier()
        }

        val imageProcessor = ImageProcessor.Builder().build()

        //Resize bitmap (300*300 because of model) and process it
        val tensorFlowImage = imageProcessor.process(
            TensorImage.fromBitmap(bitmap)
        );

        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()



        val results = detector?.detect(tensorFlowImage, imageProcessingOptions)

        val tempResults = mutableListOf<DetectedObject>();

        val widthScaleFactor = screenWidth / modelWantedWidth;
        val heightScaleFactor = screenHeight/ modelWantedHeight;

        results?.forEach {
            tempResults.add(
                DetectedObject(
                    name = it.categories.maxOf { it.label },
                    certainty = it.categories.maxOf { it.score },
                    box = RectF(
                        it.boundingBox.left,
                        it.boundingBox.top,
                        it.boundingBox.right,
                        it.boundingBox.bottom,
                    )
                )
            )
        }

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