package com.example.s501

import android.graphics.RectF

data class DetectedObject(
    val classId : Int,
    val certainty : Float,
    val box : RectF,
)
