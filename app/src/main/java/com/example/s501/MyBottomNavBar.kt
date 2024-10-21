package com.example.s501

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
fun MyBottomNavbar(onNavigate: (String) -> Unit) {
    var selectedItem by remember { mutableStateOf("Camera") }

    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavbarItem("Home", selectedItem == "Home") {
                selectedItem = "Home"
                onNavigate("Home")
            }

            NavbarItem("Camera", selectedItem == "Camera") {
                selectedItem = "Camera"
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
    MyBottomNavbar {}
}
