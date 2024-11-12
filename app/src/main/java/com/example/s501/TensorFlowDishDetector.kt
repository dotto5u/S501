package com.example.s501

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import android.util.Size
import android.view.Surface
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions
import java.util.Arrays

class TensorFlowDishDetector(
    private val context : Context,
    private var screenWidth : Float,
    private var screenHeight : Float,
) : DishDetector {

    private val modelWantedWidth = 300;
    private val modelWantedHeight = 300;

    private val maxResults : Int = 1;

    private val precisionThreshold : Float = 0.4f;

    private val modelPath = "SSDMobilenetV1.tflite"

    private var detector : ObjectDetector? = null

    private val imageProcessor = ImageProcessor
        .Builder()
        /*.add(ResizeOp(
            modelWantedWidth,
            modelWantedHeight,
            ResizeOp.ResizeMethod.BILINEAR
        ))*/
        .build();

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

    //Changes a rectF aspectRatio and resizes it from the baseSize to the targetSize
    fun scaleRect(baseSize : Size ,targetSize : Size, rectToScale : RectF) : RectF{

        //Calculating conversion values
        val targetAspectRatio = targetSize.width.toFloat() / targetSize.height.toFloat();

        //Converting the actual rectF

        val newWidth = rectToScale.height() * targetAspectRatio;

        val newHeight = rectToScale.width()/targetSize.width.toFloat() * targetSize.height.toFloat();

        val widthScaleDifference = targetSize.width.toFloat()/newWidth;
        val heightScaleDifference = targetSize.height.toFloat()/newHeight;

        val newRight = rectToScale.right/rectToScale.width() * newWidth;
        val newLeft = rectToScale.left/rectToScale.width() * newWidth;

        return RectF(
            newLeft * widthScaleDifference,
            rectToScale.top * heightScaleDifference,
            newRight * widthScaleDifference,
            rectToScale.bottom * heightScaleDifference,
        )
    }

    override fun detect(bitmap: Bitmap, rotation: Int, imageSize : Size): List<DetectedObject> {
        if (detector == null){
            setupClassifier()
        }

        //Resize bitmap (300*300 because of model) and process it
        val tensorFlowImage = imageProcessor.process(
            TensorImage.fromBitmap(bitmap)
        );

        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()

        val results = detector?.detect(tensorFlowImage, imageProcessingOptions)

        val tempResults = mutableListOf<DetectedObject>();

        val widthScaleFactor = screenWidth / imageSize.width;
        val heightScaleFactor = screenHeight / imageSize.height;

        results?.forEach {
            Log.w("OriginalBoundingBox", it.boundingBox.toString());
            tempResults.add(
                DetectedObject(
                    name = it.categories.maxOf { it.label },
                    certainty = it.categories.maxOf { it.score },
                    box = scaleRect(
                        imageSize,
                        Size(
                            screenWidth.toInt(),
                            screenHeight.toInt()
                        ),
                        RectF(
                            it.boundingBox.left * widthScaleFactor,
                            it.boundingBox.top * heightScaleFactor,
                            it.boundingBox.right * widthScaleFactor,
                            it.boundingBox.bottom * heightScaleFactor,
                        )
                    )
                )
            )
            Log.w("ResizedBoundingBox", tempResults.last().box.toString());
        }

        return tempResults;
    }

    private fun getOrientationFromRotation(rotation : Int) : ImageProcessingOptions.Orientation{
        Log.w("RotationDebug", "rotationDegrees: $rotation")
        return when(rotation){
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else -> ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }
}