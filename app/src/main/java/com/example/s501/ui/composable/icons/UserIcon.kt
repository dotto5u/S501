package com.example.s501.ui.composable.icons

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.sharp.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.s501.ui.theme.Purple40
import com.example.s501.ui.viewmodel.user.UserViewModel

@Composable
fun UserIcon(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val user = userViewModel.user.collectAsState()
    val isConnected = user.value != null

    val iconImage = if (isConnected) Icons.AutoMirrored.Filled.ExitToApp else Icons.Sharp.Person
    val iconColor = if (isConnected) Color.Red else Purple40

    IconButton(
        onClick = {
            if (isConnected) {
                userViewModel.disconnect()
                Toast.makeText(context, "Déconnexion effectuée avec succès !", Toast.LENGTH_SHORT).show()
            } else {
                navController.navigate("login")
            }
        },
        modifier = Modifier
            .padding(start = 5.dp, top = 25.dp)
            .size(40.dp)
    ) {
        Icon(
            modifier = Modifier.size(35.dp),
            imageVector = iconImage,
            contentDescription = null,
            tint = iconColor
        )
    }
}
