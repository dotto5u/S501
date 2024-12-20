package com.example.s501.data.repository

import com.example.s501.data.model.Category
import com.example.s501.data.model.Image
import com.example.s501.data.remote.ApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class ImageRepository(private val apiService: ApiService) {
    suspend fun getAll(): List<Image> {
        return apiService.getAllImages()
    }

    suspend fun uploadImage(file: File, categories: List<Category>) {
        try {
            val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)

            val categoriesJson = Gson().toJson(categories)
            val categoriesPart = categoriesJson.toRequestBody("application/json".toMediaTypeOrNull())

            apiService.uploadImage(imagePart, categoriesPart)

            println("Request sent")
        } catch (e: HttpException) {
            println("HttpException : ${e.message ?: "Unknown"}")
        } catch (e: IOException) {
            println("IOException : ${e.message ?: "Unknown"}")
        } catch (e: Exception) {
            println("Exception : ${e.message ?: "Unknown"}")
        }
    }
}