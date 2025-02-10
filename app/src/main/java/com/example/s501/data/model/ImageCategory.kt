package com.example.s501.data.model

data class ImageCategory(
    val imageId: Long,
    val userId: Int,
    val categories: List<Category>
)