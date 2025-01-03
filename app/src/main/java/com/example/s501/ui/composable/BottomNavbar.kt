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
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun BottomNavbar(currentScreen: String, onNavigate: (String) -> Unit) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavbarItem("History", currentScreen == "History") {
                onNavigate("History")
            }

            NavbarItem("Camera", currentScreen == "Camera") {
                onNavigate("Camera")
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

@Preview(showBackground = true)
@Composable
fun PreviewMyBottomNavbar() {
    BottomNavbar(currentScreen = "Screen") {}
}
