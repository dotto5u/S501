package com.example.s501.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun BottomNavbar(currentScreen: String, navController: NavHostController) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavbarItem("History", currentScreen == "History") {
                navController.navigate("history_screen")
            }

            NavbarItem("Camera", currentScreen == "Camera") {
                navController.navigate("camera_screen")
            }
        }
    }
}

@Composable
fun NavbarItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = if (isSelected) Color.Blue else Color.Gray,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(16.dp)
    )
}
