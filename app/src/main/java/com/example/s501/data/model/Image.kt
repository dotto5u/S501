package com.example.s501.data.model

data class Image(
    val id: Long,
    val userId: Int,
    val url: String,
    val categories: List<Category>
)
