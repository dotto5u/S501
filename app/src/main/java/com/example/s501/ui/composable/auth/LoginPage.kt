package com.example.s501.ui.composable.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.s501.data.model.User
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.repository.UserRepository
import com.example.s501.ui.viewmodel.auth.AuthUiState
import com.example.s501.ui.viewmodel.auth.AuthViewModel
import com.example.s501.ui.viewmodel.auth.AuthViewModelFactory
import com.example.s501.ui.viewmodel.user.UserViewModel

@Composable
fun Login(navController: NavHostController, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val apiClient = remember { ApiClient() }
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(
            repository = UserRepository(apiClient.apiService)
        )
    )

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()

    when (uiState) {
        is AuthUiState.Loading -> CircularProgressIndicator()
        is AuthUiState.Success -> {
            val user = (uiState as AuthUiState.Success).user

            userViewModel.connect(user)
            navController.navigate("camera_screen")
        }
        is AuthUiState.Error -> {
            val message = "Nom d'utilisateur ou mot de passe incorrect"

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        else -> {}
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp, start = 5.dp, end = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(start = 5.dp, top = 15.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )

                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 30.dp, vertical = 20.dp),
                shape = RoundedCornerShape(30.dp),
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
                                authViewModel.login(User(email = username, password = password))
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
                }
            }
        }
    }
}
