package com.example.s501

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.util.Log
import android.util.Size
import android.view.Surface
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import org.tensorflow.lite.task.vision.detector.ObjectDetector.ObjectDetectorOptions
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TensorFlowDishDetector(
    private val context : Context,
    private var screenWidth : Float,
    private var screenHeight : Float,
) : DishDetector {

    private val modelWantedWidth = 640;
    private val modelWantedHeight = 640;

    private val maxResults : Int = 1;

    private val precisionThreshold : Float = 0.4f;
    private val iouThreshold : Float = 0.4f;

    private val modelPath = "YoloV8_base.tflite"
    private val modelInputShape = intArrayOf(1, 3, 640, 640)
    private val modelOutputShape = intArrayOf(1, 84, 8400)

    private var interpreter : Interpreter? = null

    private val std = 255.0f;
    private val mean = 0.0f;
    private val imageProcessor = ImageProcessor
        .Builder()
        .add(ResizeOp(
            modelWantedWidth,
            modelWantedHeight,
            ResizeOp.ResizeMethod.BILINEAR
        ))
        .add(NormalizeOp(mean, std))
        .build();

    private fun setupInterpreter(){
        try{
            val options = Interpreter.Options();
            options.setNumThreads(2);
            interpreter = Interpreter(loadModelFile(context), options)
        }
        catch (e : IllegalStateException){
            e.printStackTrace()
        }
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    //Changes a rectF aspectRatio and resizes it from the baseSize to the targetSize
    private fun scaleRect(baseSize : Size ,targetSize : Size, rectToScale : RectF) : RectF{

        val widthScaleFactor = targetSize.width.toFloat()/baseSize.width.toFloat();
        val heightScaleFactor = targetSize.height.toFloat()/baseSize.height.toFloat();

        val newWidth = rectToScale.width() * widthScaleFactor;
        val newHeight = rectToScale.height() * heightScaleFactor;

        val newLeft = rectToScale.left/rectToScale.width() * newWidth;
        val newRight = rectToScale.right/rectToScale.width() * newWidth;
        val newTop = targetSize.height - rectToScale.bottom/rectToScale.height() * newHeight;
        val newBottom = targetSize.height - rectToScale.top/rectToScale.height() * newHeight;

        return RectF(
            newLeft,
            newTop,
            newRight,
            newBottom,
        )
    }



    override fun detect(bitmap: Bitmap, rotation: Int, imageSize : Size): List<DetectedObject> {
        if (interpreter == null){
            setupInterpreter();
        }

        val rotatedBitmap = rotateBitmap(bitmap, rotation);

        val tensorFlowImage = imageProcessor.process(
            TensorImage.fromBitmap(rotatedBitmap)
        );

        val inputBuffer = tensorFlowImage.buffer;

        val outputBuffer = TensorBuffer.createFixedSize(modelOutputShape, DataType.FLOAT32)

        Log.w("Inference", "Running inference")
        try {
            interpreter?.run(inputBuffer, outputBuffer.buffer.rewind())
        }
        catch (e : Exception){
            Log.e("Inference error", "Error occurred during inference")
            e.printStackTrace()
        }

        val results = decodeYOLOOutput(outputBuffer, imageSize)

        val finalResults = rescaleResults(
            results = results,
            rotation = rotation
        )

        Log.d("res", finalResults.toString())

        return finalResults;

        /*val tempResults = mutableListOf<DetectedObject>();

        Log.d("Coords", "Base : " + imageSize.width.toString() + "x" + imageSize.height.toString() + "\nTarget : " + screenWidth.toString() + "x" + screenHeight.toString())

        results?.forEach {
            Log.w("OriginalBoundingBox", it.boundingBox.toString());
            tempResults.add(
                DetectedObject(
                    name = it.categories.maxOf { it.label },
                    certainty = it.categories.maxOf { it.score },
                    box = scaleRect(
                        rotateDimensions(
                            imageSize.width,
                            imageSize.height,
                            rotation
                        ),
                        Size(
                            screenWidth.toInt(),
                            screenHeight.toInt(),
                        ),
                        rotateRectF(
                            RectF(
                                it.boundingBox.left,
                                it.boundingBox.top,
                                it.boundingBox.right,
                                it.boundingBox.bottom,
                            ),
                            rotation
                        )
                    )
                )
            )
            Log.w("ResizedBoundingBox", tempResults.last().box.toString());
        }

        return tempResults;*/
    }

    private fun rescaleResults(results : List<DetectedObject>, rotation : Int) : MutableList<DetectedObject>{
        val newResults = mutableListOf<DetectedObject>()

        for (i in results.indices) {
            val tempBox = results[i].box
            val temp = DetectedObject(
                name = results[i].name,
                certainty = results[i].certainty,
                box = scaleRect(
                    rotateDimensions(
                        modelInputShape[2],
                        modelInputShape[3],
                        rotation
                    ),
                    Size(
                        screenWidth.toInt(),
                        screenHeight.toInt()
                    ),
                    rotateRectF(
                        RectF(
                            tempBox.left,
                            tempBox.top,
                            tempBox.right,
                            tempBox.bottom
                        ),
                        rotation
                    )
                )
            )

            newResults.add(temp)
        }

        return newResults;
    }

    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        val intersectionLeft = maxOf(box1.left, box2.left)
        val intersectionTop = maxOf(box1.top, box2.top)
        val intersectionRight = minOf(box1.right, box2.right)
        val intersectionBottom = minOf(box1.bottom, box2.bottom)

        val intersectionWidth = maxOf(0f, intersectionRight - intersectionLeft)
        val intersectionHeight = maxOf(0f, intersectionBottom - intersectionTop)

        val intersectionArea = intersectionWidth * intersectionHeight
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)

        val unionArea = box1Area + box2Area - intersectionArea
        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }


    private fun applyNonMaximumSuppression(detections: List<DetectedObject>): List<DetectedObject> {
        val filteredDetections = mutableListOf<DetectedObject>()
        val sortedDetections = detections.sortedByDescending { it.certainty }

        val usedIndices = mutableSetOf<Int>()
        for (i in sortedDetections.indices) {
            if (i in usedIndices) continue

            val currentDetection = sortedDetections[i]
            filteredDetections.add(currentDetection)

            for (j in (i + 1) until sortedDetections.size) {
                if (j in usedIndices) continue
                val otherDetection = sortedDetections[j]

                val iou = calculateIoU(currentDetection.box, otherDetection.box)
                if (iou > iouThreshold) {
                    usedIndices.add(j)
                }
            }
        }

        return filteredDetections
    }


    private fun decodeYOLOOutput(outputBuffer: TensorBuffer, imageSize: Size): List<DetectedObject> {
        val detections = mutableListOf<DetectedObject>()
        val outputArray = outputBuffer.floatArray

        for (i in outputArray.indices step modelOutputShape[1]) {

            val confidence = outputArray[i + 4]
            val x = (outputArray[i] * std) + mean
            val y = (outputArray[i + 1] * std) + mean
            val width = (outputArray[i + 2] * std) + mean
            val height = (outputArray[i + 3] * std) + mean

            //Log.w("Original pixel values", "X : $x\nY : $y\nWidth : $width\nHeight : $height\n\n")

            val rect = RectF(
                (x - width / 2),
                (y - height / 2),
                (x + width / 2),
                (y + height / 2)
            )
            //Log.w("temp",rect.toString());

            val classId = argMax(outputArray, i + 5, i + modelOutputShape[1]);

            val classCertainty = outputArray[i + classId];
            val overallCertainty = classCertainty * confidence;
            if (overallCertainty >= precisionThreshold){
                detections.add(
                    DetectedObject(
                        name = "$classId",
                        certainty = confidence * classCertainty,
                        box = rect
                    )
                )
            }
        }

        detections.forEach {
            if (it.name == "63" || it.name == "62")
            {
                Log.d("IDK", it.toString())
            }
        }

        //Log.d("detections", detections.toString())
        return applyNonMaximumSuppression(detections).sortedByDescending { it.certainty }.take(maxResults)
    }

    private fun argMax(array: FloatArray, start : Int, end: Int): Int {
        var maxIdx = start
        for (i in start until end) {
            if (array[i] > array[maxIdx]) {
                maxIdx = i
            }
        }
        return maxIdx - start
    }

    //Rotates a width and height by a set rotationDegrees (from imageProxy.imageInfo)
    private fun rotateDimensions(width: Int, height: Int, rotationDegrees: Int): Size {
        return when (rotationDegrees) {
            90, 270 -> Size(
                height,
                width
            )
            180 -> Size(
                width,
                height
            )
            else -> Size(
                width,
                height
            )
        }
    }

    //Rotates rectF coordinates by a set rotationDegrees (from imageProxy.imageInfo)
    private fun rotateRectF(rect: RectF, rotationDegrees: Int): RectF {
        return when (rotationDegrees) {
            90 -> RectF(
                rect.top,
                rect.left,
                rect.bottom,
                rect.right,
            )
            180 -> RectF(
                rect.right,
                rect.bottom,
                rect.left,
                rect.top,
            )
            270 -> RectF(
                rect.top,
                rect.left,
                rect.bottom,
                rect.right,
            )
            else -> rect
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }

}