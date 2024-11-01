package com.example.s501

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.util.Log
import android.view.Surface
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Arrays

class TensorFlowDishDetector(
    private val context : Context,
    private val precisionThreshold : Float = 0.6f,
) : DishDetector {

    init {
        setupInterpreter();
    }

    private val modelPath = "yolov2-tiny-test3.tflite";

    private lateinit var interpreter : Interpreter;
    private val inputSize = 416;
    private val IoUThreshold = 0.5f;

    private fun setupInterpreter(){
        val options = Interpreter.Options().apply {
            setNumThreads(2);
        }

        try{
            interpreter = Interpreter(loadModelFile(), options);
        }
        catch (e : Exception){
            e.printStackTrace();
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath);
        val fileInputStream = assetFileDescriptor.createInputStream();
        val fileChannel = fileInputStream.channel;
        val startOffset = assetFileDescriptor.startOffset;
        val declaredLength = assetFileDescriptor.declaredLength;
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private fun calculateIoU(rectA: RectF, rectB: RectF): Float {
        //IoU formula : : Overlap/(Area A + Area B - Overlap)
        val xA = maxOf(rectA.left, rectB.left);
        val yA = maxOf(rectA.top, rectB.top);
        val xB = minOf(rectA.right, rectB.right);
        val yB = minOf(rectA.bottom, rectB.bottom);

        val overlap = maxOf(0f, xB - xA) * maxOf(0f, yB - yA);
        val areaA = (rectA.right - rectA.left) * (rectA.bottom - rectA.top);
        val areaB = (rectB.right - rectB.left) * (rectB.bottom - rectB.top);
        val unionArea = areaA + areaB - overlap;

        if (unionArea == 0f) return 0f;
        return overlap/unionArea;
    }

    //Prevents the model from detecting an object multiple times (only keeps the highest certainty)
    fun nonMaxSuppression(
        detections: List<DetectedObject>,
        //IoU is used to check if two boxes overlap. This threshold defines at which level of overlap we start to consider that the same object was detected twice.
    ): List<DetectedObject> {
        //Sorts DetectedObjects by certainty
        var sortedObjects = detections.sortedByDescending { it.certainty }
        val selectedObjects = mutableListOf<DetectedObject>()

        while (sortedObjects.isNotEmpty()) {
            //Picks the detected object with the highest certainty
            val bestDetection = sortedObjects.first()
            selectedObjects.add(bestDetection)
            val remainingObjects = mutableListOf<DetectedObject>()

            //Compares this detected area with previous ones
            for (detectedObject in sortedObjects.drop(1)) {
                val iou = calculateIoU(bestDetection.box, detectedObject.box)
                //Filters IoU by the given threshold
                if (iou < IoUThreshold) {
                    remainingObjects.add(detectedObject)
                }
            }

            //Updates the list with remaining objects after filtering
            sortedObjects = remainingObjects
        }

        return selectedObjects
    }

    private fun processYOLOOutput(outputData: Array<Array<Array<FloatArray>>>): List<DetectedObject> {
        val results = mutableListOf<DetectedObject>();
        val certaintyThreshold = 0.5f;

        for (i in outputData.indices) {
            for (j in outputData[i].indices){
                for (dectectedObject in outputData[i][j]){
                    val certainty = dectectedObject[4];
                    if (certainty > certaintyThreshold) {
                        val classId = dectectedObject.sliceArray(5 until dectectedObject.size).indexOfFirst { it > certaintyThreshold }
                        val certaintyScore = dectectedObject[4] * dectectedObject[classId + 5]
                        val box = RectF(
                            dectectedObject[0] - dectectedObject[2] / 2,
                            dectectedObject[1] - dectectedObject[3] / 2,
                            dectectedObject[0] + dectectedObject[2] / 2,
                            dectectedObject[1] + dectectedObject[3] / 2,
                        )
                        results.add(DetectedObject(classId, certaintyScore, box))
                    }
                }
            }
        }

        return nonMaxSuppression(results)
    }



    override fun detect(bitmap: Bitmap, rotation : Int): List<DetectedObject> {
        if (!::interpreter.isInitialized){
            setupInterpreter()
        }


        val byteBufferImage = processBitmap(bitmap, rotation)

        val tensorShape = interpreter.getOutputTensor(0).shape();  //returns the tensor shape at index 0 (because our model only has 1 tensor) in the form of an array of ints

        Log.w("OutputTensor", "Output tensor shape: ${tensorShape.contentToString()}")

        //4D array to receive output data, will need refactoring because currently it isn't automatic, it needs hardcoded size values (which will change when we will add labels to the model)
        val outputData = Array(1) { Array(13) { Array(13) { FloatArray(425) } } }

        Log.w("InputBuffer", "Input buffer size: ${byteBufferImage.capacity()}");

        Log.w("Interpreter", "Running inference");
        try {
            interpreter.run(byteBufferImage, outputData);
        } catch (e : Exception){
            Log.e("Interpreter", "Error occurred during inference: ${e.message}");
            e.printStackTrace()
            throw Exception("Error occurred during inference, shutting down");
        }
        Log.w("Interpreter", "Inference complete.");


        //Post-process the model output
        return processYOLOOutput(outputData);
    }

    //Rotates a bitmap using a rotation
    fun rotateBitmap(bitmap: Bitmap, rotation: Int): Bitmap {
        val matrix = Matrix()
        when (rotation) {
            Surface.ROTATION_90 -> matrix.postRotate(90f);
            Surface.ROTATION_270 -> matrix.postRotate(270f);
            Surface.ROTATION_180 -> matrix.postRotate(180f);
            else -> {}
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true);
    }

    //Rotates the image converted to a bitmap and returns it as a ByteBuffer to be processed my the model
    private fun processBitmap(bitmap : Bitmap, rotation: Int) : ByteBuffer{
        val rotatedBitmap = rotateBitmap(bitmap, rotation);

        //Scales the bitmap to the model's awaited dimensions
        val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, inputSize, inputSize, true);


        val inputBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3);
        inputBuffer.order(ByteOrder.nativeOrder());

        val pixels = IntArray(inputSize * inputSize);
        scaledBitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize);

        for (pixel in pixels) {
            //Normalizes RGB values
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }

        inputBuffer.rewind()
        return inputBuffer
    }
}