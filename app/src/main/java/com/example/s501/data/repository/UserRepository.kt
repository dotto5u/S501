package com.example.s501.data.repository

import android.util.Log
import com.example.s501.data.model.User
import com.example.s501.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

class UserRepository(private val apiService: ApiService) {
    suspend fun registerUser(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val responseBody: ResponseBody = apiService.registerUser(user)
                val jsonResponse = responseBody.string()

                Log.d("UserRepository", "Register Response : $jsonResponse")
                jsonResponse.contains("success")
            } catch (e: HttpException) {
                Log.e("UserRepository", "HttpException : ${e.code()} - ${e.message()}")
                false
            } catch (e: IOException) {
                Log.e("UserRepository", "IOException : ${e.message}")
                false
            }
        }
    }

    suspend fun loginUser(user: User): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val responseBody: ResponseBody = apiService.loginUser(user)
                val jsonResponse = responseBody.string()

                Log.d("UserRepository", "Login Response : $jsonResponse")
                jsonResponse.contains("success")
            } catch (e: HttpException) {
                Log.e("UserRepository", "HttpException : ${e.code()} - ${e.message()}")
                false
            } catch (e: IOException) {
                Log.e("UserRepository", "IOException : ${e.message}")
                false
            }
        }
    }
}
