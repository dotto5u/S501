package com.example.s501.ui.composable.login

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.s501.data.model.User
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.repository.UserRepository
import com.example.s501.ui.viewmodel.auth.AuthUiState
import com.example.s501.ui.viewmodel.auth.AuthViewModel
import com.example.s501.ui.viewmodel.auth.AuthViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(UserRepository(ApiClient().apiService)))) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by authViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.padding(30.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Login",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6200EA)
                )
                Spacer(modifier = Modifier.height(40.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = {
                        if (username.isNotEmpty() && password.isNotEmpty()) {
                            coroutineScope.launch {
                                authViewModel.login(User(email = username, password = password))
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(text = "Login", fontSize = 18.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(20.dp))
                when (uiState) {
                    is AuthUiState.Loading -> CircularProgressIndicator()
                    is AuthUiState.Success -> {
                        val intent = Intent("com.example.s501.MainActivity")
                        launcher.launch(intent)
                    }
                    is AuthUiState.Error -> {
                        Text("Nom d'utilisateur ou mot de passe incorrect", color = Color.Red)
                    }
                    else -> {}
                }
            }
        }
    }
}
