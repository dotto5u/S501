package com.example.s501.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.s501.data.model.Category
import com.example.s501.data.model.Image
import com.example.s501.data.remote.ApiService
import com.google.gson.Gson
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class ImageRepository(private val apiService: ApiService) {
    suspend fun getAll(): List<Image> {
        return apiService.getAllImages()
    }

    suspend fun uploadImage(bitmap : Bitmap, categories: List<Category>) {
        Log.d("Upload attempt", "An image appload attempt")
        try {
            val byteArrayOutputSteam = ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputSteam);
            val byteArray = byteArrayOutputSteam.toByteArray();

            val requestBody = RequestBody.create("image/jpg".toMediaType(), byteArray);
            val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

            val categoriesJson = Gson().toJson(categories)
            val categoriesPart = categoriesJson.toRequestBody("application/json".toMediaTypeOrNull())

            val responseBody = apiService.uploadImage(imagePart, categoriesPart)
            val jsonResponse = responseBody.string()
            Log.w("JSON Response", jsonResponse.toString());
        } catch (e: HttpException) {
            Log.e("HttpException","${e.response()?.errorBody()?.string()}")
            null
        } catch (e: IOException) {
            Log.e("IOException","${e.message ?: "Unknown"}")
            null
        } catch (e: Exception) {
            Log.e("Exception", "${e.message ?: "Unknown"}")
            null
        }
    }
}