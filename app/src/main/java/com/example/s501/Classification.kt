package com.example.s501

import android.graphics.RectF

data class Classification(
    val name : String,
    val certainty : Float,
    val box : RectF
)
