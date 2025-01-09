package com.example.s501.data.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import android.util.Size
import com.example.s501.data.model.DetectedObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class TensorFlowDishDetector(
    private val context : Context,
    private var screenWidth : Float,
    private var screenHeight : Float,
) : DishDetector {

    //Detection parameters
    private val maxResults : Int = 1;
    private val precisionThreshold : Float = 0.3f;
    private val iouThreshold : Float = 0.4f;

    //File paths
    private val labelPath = "labelmap.txt";
    private val modelPath = "YoloV8_trained.tflite";

    //Model variables - Do not touch
    private var modelInputShape : IntArray? = null;
    private var modelOutputShape : IntArray? = null;

    private val labels = mutableListOf<String>()

    private var interpreter : Interpreter? = null;

    //Image parameters
    private val std = 255.0f;
    private val mean = 0.0f;
    private val imageProcessor = ImageProcessor
        .Builder()
        .add(NormalizeOp(mean, std))
        .add(CastOp(DataType.FLOAT32))
        .build();

    init {
        setupInterpreter();
        readLabels();
    }

    private fun setupInterpreter(){
        try{
            val options = Interpreter.Options();
            options.setNumThreads(4);

            val model = FileUtil.loadMappedFile(context, modelPath);
            interpreter = Interpreter(model, options);

            modelInputShape = interpreter?.getInputTensor(0)?.shape();
            modelOutputShape = interpreter?.getOutputTensor(0)?.shape();
        }
        catch (e : IllegalStateException){
            e.printStackTrace()
        }
    }

    private fun readLabels() {
        try {
            val inputStream: InputStream = context.assets.open(labelPath);
            val reader = BufferedReader(InputStreamReader(inputStream));

            var line: String? = reader.readLine();
            while (line != null && line != "") {
                labels.add(line);
                line = reader.readLine();
            }
            reader.close();
            inputStream.close();

        } catch (e: IOException) {
            e.printStackTrace();
        }
    }

    override fun detect(bitmap: Bitmap, rotation: Int, imageSize : Size): List<DetectedObject> {
        if (interpreter == null){
            setupInterpreter();
        }

        if (modelInputShape == null){
            throw Exception("Model input shape is null after interpreter was setup");
        }
        if (modelOutputShape == null){
            throw Exception("Model output shape is null after interpreter was setup");
        }

        val rotatedBitmap = rotateBitmap(bitmap, rotation);
        val resizedBitmap = Bitmap.createScaledBitmap(
            rotatedBitmap,
            modelInputShape!![1],
            modelInputShape!![2],
            false
        );

        val tensorFlowImage = TensorImage(DataType.FLOAT32);
        tensorFlowImage.load(resizedBitmap);
        val processedImage = imageProcessor.process(tensorFlowImage);

        val inputBuffer = processedImage.buffer;
        val outputBuffer = TensorBuffer.createFixedSize(modelOutputShape, DataType.FLOAT32);

        Log.d("Inference", "Running inference")
        try {
            interpreter?.run(inputBuffer, outputBuffer.buffer)
        }
        catch (e : Exception){
            Log.e("Inference error", "Error occurred during inference")
            e.printStackTrace()
        }

        val results = decodeYOLOOutput(outputBuffer.floatArray)

        val finalResults = rescaleResults(
            results = results
        )

        return finalResults;
    }

    private fun rescaleResults(results : List<DetectedObject>) : MutableList<DetectedObject>{
        val newResults = mutableListOf<DetectedObject>()

        results.forEach {
            newResults.add(
                DetectedObject(
                    name = it.name,
                    certainty = it.certainty,
                    box = RectF(
                        it.box.left*screenWidth,
                        it.box.top*screenHeight,
                        it.box.right*screenWidth,
                        it.box.bottom*screenHeight
                    )
                )
            )
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


    private fun applyNonMaxSuppression(detections: List<DetectedObject>) : MutableList<DetectedObject> {
        val sortedDetections = detections.sortedByDescending { it.certainty }.toMutableList()
        val filteredDetections = mutableListOf<DetectedObject>()

        while(sortedDetections.isNotEmpty()) {
            val first = sortedDetections.first();
            filteredDetections.add(first);
            sortedDetections.remove(first);

            val iterator = sortedDetections.iterator();
            while (iterator.hasNext()) {
                val nextDetection = iterator.next();
                val iou = calculateIoU(first.box, nextDetection.box);
                if (iou >= iouThreshold) {
                    iterator.remove();
                }
            }
        }

        return filteredDetections;
    }


    private fun decodeYOLOOutput(array: FloatArray) : List<DetectedObject> {
        val detections = mutableListOf<DetectedObject>()

        for (i in 0 until modelOutputShape!![2]) {
            var maxConf = precisionThreshold;
            var maxId = -1;
            var j = 4;
            var arrayId = i + modelOutputShape!![2] * j
            while (j < modelOutputShape!![1]){
                if (array[arrayId] > maxConf) {
                    maxConf = array[arrayId]
                    maxId = j - 4;
                }
                j+= 1;
                arrayId += modelOutputShape!![2];
            }

            if (maxConf > precisionThreshold) {
                val clsName = labels[maxId];
                val xCenter = array[i];
                val yCenter = array[i + modelOutputShape!![2]];

                val width = array[i + modelOutputShape!![2] * 2]
                val height = array[i + modelOutputShape!![2] * 3]

                val left = xCenter - (width/2F);
                val top = yCenter - (height/2F);
                val right = xCenter + (width/2F);
                val bottom = yCenter + (height/2F);
                if (left < 0F || left > 1F) continue;
                if (top < 0F || top > 1F) continue;
                if (right < 0F || right > 1F) continue;
                if (bottom < 0F || bottom > 1F) continue;

                detections.add(
                    DetectedObject(
                        name = clsName,
                        certainty = maxConf,
                        box = RectF(
                            left,
                            top,
                            right,
                            bottom
                        )
                    )
                )
            }
        }

        return applyNonMaxSuppression(detections).sortedByDescending { it.certainty }.take(maxResults);
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotationDegrees.toFloat())
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }
}