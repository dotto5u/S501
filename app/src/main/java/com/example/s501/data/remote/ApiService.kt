package com.example.s501.data.remote

import com.example.s501.data.model.Image
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @GET("image/all")
    suspend fun getOnlineImages(): List<Image>

    @GET("image/{image_id}/get")
    suspend fun getOnlineImage(@Path("image_id") imageId: String): Image

    @Multipart
    @POST("image/upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("imageCategory") imageCategory: RequestBody
    ): ResponseBody

    @DELETE("image/{image_id}/delete")
    suspend fun deleteImage(@Path("image_id") imageId: String): ResponseBody
}