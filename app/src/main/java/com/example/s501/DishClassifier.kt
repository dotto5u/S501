package com.example.s501

import android.graphics.Bitmap

interface DishClassifier {
    fun classify(bitmap : Bitmap, rotation : Int) : List<Classification>
}