package com.example.s501.data.repository

import com.example.s501.data.model.Image
import com.example.s501.data.remote.ApiService

class ImageRepository(private val apiService: ApiService) {
    suspend fun getAll(): List<Image> {
        return apiService.getAllImages()
    }
}