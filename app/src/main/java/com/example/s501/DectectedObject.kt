package com.example.s501

import android.graphics.RectF

data class DetectedObject(
    val name : String,
    val certainty : Float,
    val box : RectF,
)
{
    override fun toString(): String {
        return "DetectedObject(name=$name, certainty=$certainty, box=${box.toShortString()})"
    }
}
