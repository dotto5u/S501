package com.example.s501.ui.composable.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.s501.R
import com.example.s501.ui.theme.Purple80

@Composable
fun HistoryFilter(isLocal: MutableState<Boolean>, isClicked: MutableState<Int>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HistoryPill(
            text = stringResource(R.string.history_filter_pill_local),
            isSelected = isLocal.value,
            onClick = {
                isLocal.value = true
                isClicked.value++
            }
        )
        HistoryPill(
            text = stringResource(R.string.history_filter_pill_online),
            isSelected = !isLocal.value,
            onClick = {
                isLocal.value = false
                isClicked.value++
            }
        )
    }
}

@Composable
fun HistoryPill(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .height(40.dp)
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (isSelected) Purple80 else Color.Gray),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Purple80 else Color.White,
            contentColor = Color.Black
        ),
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.DarkGray
        )
    }
}
