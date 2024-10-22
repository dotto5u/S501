package com.example.s501

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.s501.ui.theme.LightPurple

@Composable
fun History() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        HistoryHeader()
        Spacer(
            modifier = Modifier.
            height(25.dp)
        )
        HistoryContent()
    }
}

@Composable
fun HistoryHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.history_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(
            modifier = Modifier
                .height(25.dp)
        )
        HistoryFilter()
    }
}

@Composable
fun HistoryFilter() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .padding(end = 8.dp),
            text = stringResource(R.string.history_filter_text),
            fontSize = 14.sp
        )
        FilterButton(
            buttonText = R.string.history_filter_button_text_category
        )
    }
}

// TODO implémenter la fonctionnalité du filtre
@Composable
fun FilterButton(buttonText: Int) {
    var buttonClicked by remember { mutableStateOf(false) }

    Button(
        onClick = {
            buttonClicked = !buttonClicked
        },
        modifier = Modifier
            .padding(end = 8.dp)
            .height(34.dp)
            .defaultMinSize(34.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp,  if (buttonClicked) LightPurple else Color.Gray),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (buttonClicked) LightPurple else Color.White,
            contentColor = Color.Black
        ),
    ) {
        Text(
            text = stringResource(buttonText),
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun HistoryContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
    ) {
        // TODO valeurs statiques à changer
        HistoryItem(
            image = R.drawable.clementine,
            title = "Clémentine",
            category = "Fruit"
        )
        Spacer(
            modifier = Modifier
                .height(30.dp)
        )
        HistoryItem(
            image = R.drawable.bolognaise,
            title = "Pâtes à la bolognaise",
            category = "Plats à base de pâtes"
        )
        Spacer(
            modifier = Modifier
                .height(30.dp)
        )
        HistoryItem(
            image = R.drawable.abricot,
            title = "Abricot",
            category = "Fruit"
        )

    }
}

@Composable
fun HistoryItem(
    image: Int,
    title: String,
    category: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // TODO revoir la façon dont la taille des images est géré
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .height(125.dp)
                .width(150.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(
            modifier = Modifier
                .width(15.dp)
        )
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier = Modifier
                    .height(5.dp)
            )
            Text(
                text = category,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    History()
}