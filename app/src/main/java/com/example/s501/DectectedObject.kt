package com.example.s501

import android.graphics.RectF

data class DetectedObject(
    val classId : Int,
    val certainty : Float,
    val box : RectF,
)
{
    override fun toString(): String {
        return "DetectedObject(classId=$classId, certainty=$certainty, box=${box.toShortString()})"
    }
}
