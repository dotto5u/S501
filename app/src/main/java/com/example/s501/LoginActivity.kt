package com.example.s501

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.s501.data.model.User
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.repository.UserRepository
import com.example.s501.ui.viewmodel.auth.AuthUiState
import com.example.s501.ui.viewmodel.auth.AuthViewModel
import com.example.s501.ui.viewmodel.auth.AuthViewModelFactory
import kotlinx.coroutines.launch

// TODO à adapter
class LoginActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels {
        val apiClient = ApiClient()
        val userRepository = UserRepository(apiClient.apiService)

        AuthViewModelFactory(userRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.loginButton)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is AuthUiState.Idle -> {
                            // TODO
                        }
                        is AuthUiState.Loading -> {
                            // TODO
                        }
                        is AuthUiState.Success -> {
                            navigateToMainActivity()
                        }
                        is AuthUiState.Error -> {
                            usernameEditText.error = "Nom d'utilisateur ou mot de passe incorrect"
                        }
                    }
                }
            }
        }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val user = User(email = username, password = password)

                authViewModel.login(user)
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
