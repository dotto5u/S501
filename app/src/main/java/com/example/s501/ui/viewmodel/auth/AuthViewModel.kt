package com.example.s501.ui.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.s501.data.model.User
import com.example.s501.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class AuthViewModel(private val repository: UserRepository) : ViewModel() {
    private var job: Job? = null
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(user: User) {
        job?.cancel()

        job = viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading

                val response: User? = repository.registerUser(user)

                if (response != null) {
                    _uiState.value = AuthUiState.Success(response)
                } else {
                    _uiState.value = AuthUiState.Error
                }
            } catch (e: CancellationException) {
                Log.d("AuthViewModel", "Coroutine cancelled")
                throw e
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error
                Log.e("AuthViewModel", e.message ?: "Unknown error")
            }
        }
    }

    fun login(user: User) {
        job?.cancel()

        job = viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading

                val response: User? = repository.loginUser(user)

                if (response != null) {
                    _uiState.value = AuthUiState.Success(response)
                } else {
                    _uiState.value = AuthUiState.Error
                }
            } catch (e: CancellationException) {
                Log.d("AuthViewModel", "Coroutine cancelled")
                throw e
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error
                Log.e("AuthViewModel", e.message ?: "Unknown error")
            }
        }
    }
}
