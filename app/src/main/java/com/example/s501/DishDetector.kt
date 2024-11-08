package com.example.s501

import android.graphics.Bitmap

interface DishDetector {
    fun detect(bitmap : Bitmap, rotation : Int) : List<DetectedObject>
}