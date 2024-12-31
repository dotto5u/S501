package com.example.s501.data.analysis

import android.graphics.Bitmap
import android.util.Size
import com.example.s501.data.model.DetectedObject

interface DishDetector {
    fun detect(bitmap : Bitmap, rotation : Int, imageSize : Size) : List<DetectedObject>
}