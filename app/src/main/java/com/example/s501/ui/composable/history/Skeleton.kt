package com.example.s501.ui.composable.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonLoading(count: Int) {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(count) {
            SkeletonItem()
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SkeletonItem() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .height(125.dp)
                    .width(150.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.width(15.dp))
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.6f)
                        .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.4f)
                        .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.4f)
                        .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
                )
            }
        }
    }
}