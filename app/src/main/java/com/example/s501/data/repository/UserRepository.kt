package com.example.s501.data.repository

import android.util.Log
import com.example.s501.data.model.User
import com.example.s501.data.remote.ApiService
import retrofit2.HttpException
import java.io.IOException

class UserRepository(private val apiService: ApiService) {
    suspend fun registerUser(user: User): User? {
        return try {
            val response: User = apiService.registerUser(user)

            if (response.id != 0 && response.email.isNotEmpty()) {
                response
            } else {
                null
            }
        } catch (e: HttpException) {
            Log.e("UserRepository", "HttpException : ${e.code()} - ${e.message()}")
            null
        } catch (e: IOException) {
            Log.e("UserRepository", "IOException : ${e.message}")
            null
        }
    }

    suspend fun loginUser(user: User): User? {
        return try {
            val response: User = apiService.loginUser(user)

            if (response.id != 0 && response.email.isNotEmpty()) {
                response
            } else {
                null
            }
        } catch (e: HttpException) {
            Log.e("UserRepository", "HttpException : ${e.code()} - ${e.message()}")
            null
        } catch (e: IOException) {
            Log.e("UserRepository", "IOException : ${e.message}")
            null
        }
    }
}
