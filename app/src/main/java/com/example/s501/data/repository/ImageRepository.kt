package com.example.s501.data.repository

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.s501.data.json.JsonFileService
import com.example.s501.data.model.Image
import com.example.s501.data.model.ImageCategory
import com.example.s501.data.remote.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class ImageRepository(
    private val context: Context,
    private val apiService: ApiService,
    private val jsonFileService: JsonFileService
) {
    suspend fun getLocalImages(): List<Image> {
        return withContext(Dispatchers.IO) {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            val images = mutableListOf<Image>()

            cursor?.use { c ->
                while (c.moveToNext()) {
                    val imageId = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                    val imagePath = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))

                    val imageUrl = "file://$imagePath"
                    val categories = jsonFileService.getCategoriesFromJsonFile(imageId)

                    val image = Image(
                        id = imageId.toInt(),
                        url = imageUrl,
                        categories = categories
                    )

                    images.add(image)
                }
            }

            images
        }
    }

    suspend fun getOnlineImages(): List<Image> {
        return apiService.getOnlineImages()
    }

    suspend fun getOnlineImage(imageId: String): Image? {
        return try {
            val image = apiService.getOnlineImage(imageId)

            if (image.id != 0 && image.url.isNotEmpty() && image.categories.isNotEmpty()) {
                image
            } else {
                null
            }
        } catch (e: HttpException) {
            Log.e("ImageRepository", "HttpException : HTTP ${e.code()} - ${e.message}")
            null
        } catch (e: IOException) {
            Log.e("ImageRepository", "IOException : ${e.message ?: "Unknown IO error"}")
            null
        } catch (e: Exception) {
            Log.e("ImageRepository", "Exception : ${e.message ?: "Unknown error"}")
            null
        }
    }

    suspend fun uploadImage(file: File, imageCategory: ImageCategory): Boolean {
        return try {
            val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)

            val categoriesJson = Gson().toJson(imageCategory)
            val categoriesPart = categoriesJson.toRequestBody("application/json".toMediaTypeOrNull())

            val responseBody = apiService.uploadImage(imagePart, categoriesPart)
            val jsonResponse = responseBody.string()

            Log.d("ImageRepository", "API Response : $jsonResponse")
            jsonResponse.contains("success")
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()

            Log.e("ImageRepository", "HttpException : HTTP ${e.code()} - ${errorBody ?: "No error body"}")
            false
        } catch (e: IOException) {
            Log.e("ImageRepository", "IOException : ${e.message ?: "Unknown IO error"}")
            false
        } catch (e: Exception) {
            Log.e("ImageRepository", "Exception : ${e.message ?: "Unknown error"}")
            false
        }
    }

    suspend fun deleteImage(imageId: String): Boolean {
        return try {
            val responseBody = apiService.deleteImage(imageId)
            val jsonResponse = responseBody.string()

            Log.d("ImageRepository", "API Response : $jsonResponse")
            jsonResponse.contains("success")
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()

            Log.e("ImageRepository", "HttpException : HTTP ${e.code()} - ${errorBody ?: "No error body"}")
            false
        } catch (e: IOException) {
            Log.e("ImageRepository", "IOException : ${e.message ?: "Unknown IO error"}")
            false
        } catch (e: Exception) {
            Log.e("ImageRepository", "Exception : ${e.message ?: "Unknown error"}")
            false
        }
    }
}