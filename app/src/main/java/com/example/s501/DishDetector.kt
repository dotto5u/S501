package com.example.s501

import android.graphics.Bitmap
import android.util.Size

interface DishDetector {
    fun detect(bitmap : Bitmap, rotation : Int, imageSize : Size) : List<DetectedObject>
}