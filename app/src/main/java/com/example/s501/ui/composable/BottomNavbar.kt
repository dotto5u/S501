package com.example.s501.ui.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CameraAlt
import com.example.s501.ui.theme.Purple40

@Composable
fun BottomNavbar(currentScreen: String, navController: NavHostController) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavbarItem(
                label = "History",
                isSelected = currentScreen == "History",
                icon = Icons.Default.History
            ) {
                navController.navigate("history_screen")
            }

            NavbarItem(
                label = "Camera",
                isSelected = currentScreen == "Camera",
                icon = Icons.Default.CameraAlt
            ) {
                navController.navigate("camera_screen")
            }
        }
    }
}

@Composable
fun NavbarItem(label: String, isSelected: Boolean, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label Icon",
            tint = if (isSelected) Purple40 else Color.Gray
        )

        Text(
            text = label,
            color = if (isSelected) Purple40 else Color.Gray,
            fontSize = 12.sp
        )
    }
}
