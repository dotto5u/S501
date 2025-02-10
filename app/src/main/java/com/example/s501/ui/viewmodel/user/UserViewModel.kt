package com.example.s501.ui.viewmodel.user

import androidx.lifecycle.ViewModel
import com.example.s501.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    fun connect(user: User) {
        _user.value = user
    }

    fun disconnect() {
        _user.value = null
    }
}
