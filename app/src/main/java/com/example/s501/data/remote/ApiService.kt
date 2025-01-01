package com.example.s501.data.remote

import com.example.s501.data.model.Image
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @GET("image/all")
    suspend fun getAllImages(): List<Image>

    @Multipart
    @POST("image/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("categories") categories: RequestBody
    ): ResponseBody
}