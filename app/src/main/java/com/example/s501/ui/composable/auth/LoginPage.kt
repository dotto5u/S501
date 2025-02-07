package com.example.s501.ui.composable.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.s501.data.model.User
import com.example.s501.data.remote.ApiClient
import com.example.s501.data.repository.UserRepository
import com.example.s501.ui.composable.icons.BackIcon
import com.example.s501.ui.theme.Purple
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

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by authViewModel.uiState.collectAsState()
    val isLoading = uiState is AuthUiState.Loading

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                val user = (uiState as AuthUiState.Success).user

                Toast.makeText(context, "Connexion effectuée avec succès !", Toast.LENGTH_SHORT).show()
                userViewModel.connect(user)
                navController.popBackStack()
            }
            is AuthUiState.Error -> {
                val message = "Email ou mot de passe incorrect"

                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
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
                BackIcon(navController)
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
                        text = "Connexion",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Purple
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mot de passe") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                authViewModel.login(
                                    User(
                                        email = email.trim(),
                                        password = password.trim()
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Purple),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Connexion",
                                fontSize = 18.sp,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
