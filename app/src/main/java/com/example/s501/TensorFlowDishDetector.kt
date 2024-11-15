package com.example.s501

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.collection.IntIntPair
import androidx.core.graphics.times
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
    /*fun scaleRect(baseSize : Size ,targetSize : Size, rectToScale : RectF) : RectF{

        //Calculating conversion values
        val targetAspectRatio = targetSize.width.toFloat() / targetSize.height.toFloat();

        //Converting the actual rectF
        val newWidth = rectToScale.height() * targetAspectRatio;

        Log.d("TestDimensions", newWidth.toString())

        val widthScaleFactor = targetSize.width.toFloat()/newWidth;
        val heightScaleFactor = targetSize.height.toFloat()/baseSize.height;

        val newRight = rectToScale.right/baseSize.width * newWidth;
        val newLeft = rectToScale.left/baseSize.width * newWidth;

        return RectF(
            newLeft * widthScaleFactor,
            rectToScale.top * heightScaleFactor,
            newRight * widthScaleFactor,
            rectToScale.bottom * heightScaleFactor,
        )
    }*/

    fun scaleRect(baseSize : Size ,targetSize : Size, rectToScale : RectF) : RectF{

        val widthScaleFactor = targetSize.width.toFloat()/baseSize.width.toFloat();
        val heightScaleFactor = targetSize.height.toFloat()/baseSize.height.toFloat();

        val newWidth = rectToScale.width() * widthScaleFactor;
        val newHeight = rectToScale.height() * heightScaleFactor;

        val newLeft =  rectToScale.left/rectToScale.width() * newWidth;
        val newRight = rectToScale.right/rectToScale.width() * newWidth;
        val newTop = rectToScale.top/rectToScale.height() * newHeight;
        val newBottom = rectToScale.bottom/rectToScale.height() * newHeight;

        return RectF(
            newLeft,
            newTop,
            newRight,
            newBottom,
        )
    }

    /*fun scaleRect(baseSize : Size ,targetSize : Size, rectToScale : RectF) : RectF{

        val widthScaleFactor = targetSize.width.toFloat()/baseSize.width.toFloat();
        val heightScaleFactor = targetSize.height.toFloat()/baseSize.height.toFloat();

        val newLeft = rectToScale.left * widthScaleFactor;
        val newRight = rectToScale.right * widthScaleFactor;
        val newTop = rectToScale.top * heightScaleFactor;
        val newBottom = rectToScale.bottom * heightScaleFactor;

        return RectF(
            newLeft,
            newTop,
            newRight,
            newBottom,
        )
    }*/



    override fun detect(bitmap: Bitmap, rotation: Int, imageSize : Size): List<DetectedObject> {
        if (detector == null){
            setupClassifier()
        }

        val tensorFlowImage = imageProcessor.process(
            TensorImage.fromBitmap(bitmap)
        );

        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()

        val results = detector?.detect(tensorFlowImage, imageProcessingOptions)

        val tempResults = mutableListOf<DetectedObject>();

        Log.d("Coords", "Base : " + imageSize.width.toString() + "x" + imageSize.height.toString() + "\nTarget : " + screenWidth.toString() + "x" + screenHeight.toString())

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
                            it.boundingBox.left,
                            it.boundingBox.top,
                            it.boundingBox.right,
                            it.boundingBox.bottom,
                        )
                    )
                )
            )
            Log.w("ResizedBoundingBox", tempResults.last().box.toString());
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