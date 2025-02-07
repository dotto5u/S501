package com.example.s501.ui.composable.icons

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun BackIcon(navController: NavHostController, route: String? = null) {
    IconButton(
        onClick = {
            if (route.isNullOrEmpty() || !navController.popBackStack(route, false)) {
                navController.popBackStack()
            } else {
                navController.navigate(route)
            }
        },
        modifier = Modifier.padding(start = 5.dp, top = 15.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null
        )
    }
}