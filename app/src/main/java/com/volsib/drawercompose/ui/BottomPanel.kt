package com.volsib.drawercompose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomPanel(
    onColorClick: (Color) -> Unit,
    onLineWidthChange: (Float) -> Unit,
    onTransparencyChange: (Float) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ColorList { color ->
            onColorClick(color)
        }
        LineWidthSlider { lineWidth ->
            onLineWidthChange(lineWidth)
        }
        TransparencySlider {transparency ->
            onTransparencyChange(transparency)
        }
        ButtonPanel(
            onBackClick = onBackClick,
            onSaveClick = onSaveClick
        )
        Spacer(modifier = Modifier.height(5.dp))
    }
}

@Composable
fun ColorList(onClick: (Color) -> Unit) {
    val colors = listOf(
        Color.Blue,
        Color.Red,
        Color.Black,
        Color.Magenta,
        Color.Yellow,
        Color.Green,
        Color.DarkGray,
        Color.Cyan
    )

    LazyRow(modifier = Modifier.padding(10.dp)) {
        items(colors) { color ->
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .clickable {
                        onClick(color)
                    }
                    .size(40.dp)
                    .background(color, CircleShape)
            ) {

            }
        }
    }
}

@Composable
fun LineWidthSlider(onChange: (Float) -> Unit) {
    var position by remember {
        mutableFloatStateOf(0.05f)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Line width: ${(position * 100).toInt()}")
        Slider(
            value = position,
            onValueChange = {
                val temPosition = if (it > 0) it else 0.01f
                position = temPosition
                onChange(temPosition * 100)
            }
        )
    }
}

@Composable
fun TransparencySlider(onChange: (Float) -> Unit) {
    var position by remember {
        mutableFloatStateOf(0f)
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Transparency: ${(position * 100).toInt()}%")
        Slider(
            value = position,
            onValueChange = {
                val temPosition = if (it < 0.95f) it else 0.95f
                position = temPosition
                onChange(temPosition * 100)
            }
        )
    }
}

@Composable
fun ButtonPanel(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White),

            onClick = { onBackClick() }
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null
            )
        }
        IconButton(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White),

            onClick = { onSaveClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null
            )
        }
    }
}