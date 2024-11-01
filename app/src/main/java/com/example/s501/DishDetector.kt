package com.example.s501

import android.graphics.Bitmap
import java.nio.ByteBuffer

interface DishDetector {
    fun detect(bitmap: Bitmap, rotation : Int) : List<DetectedObject>
}