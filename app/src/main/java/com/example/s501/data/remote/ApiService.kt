package com.example.s501.data.remote

import com.example.s501.data.model.Image
import retrofit2.http.GET

interface ApiService {
    // pour tester, futur endpoint : object/all
    @GET("c/08d2-5f16-4da7-bf46")
    suspend fun getAllImages(): List<Image>
}