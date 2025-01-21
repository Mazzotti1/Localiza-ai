package com.hatlas.ui.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hatlas.ui.ViewModel.MenuScreenViewModel

@Composable
fun FilterButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isVisible: Boolean,
    viewModel : MenuScreenViewModel
) {
    val radiusListFilter = listOf("Padrão", "300m", "600m", "800m")
    val selectedRadius = viewModel.selectedRadiusFilter
    var sliderPosition by remember { mutableStateOf(radiusListFilter.indexOf(selectedRadius.value).toFloat()) }

    Box(
        modifier = modifier
            .size(56.dp)
            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
            .border(2.dp, MaterialTheme.colorScheme.primary, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.FilterAlt,
            contentDescription = "Filter distance",
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
    }

    AnimatedVisibility(visible = isVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .height(200.dp)
                .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(30.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Distância",
                    fontSize = 26.sp,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    radiusListFilter.forEachIndexed { index, radius ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    if (selectedRadius.value == radius) Color.Blue else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable {
                                    selectedRadius.value = radius
                                    sliderPosition = index.toFloat()
                                }
                        )
                        Text(
                            text = radius,
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        selectedRadius.value = radiusListFilter[it.toInt()]
                    },
                    valueRange = 0f..(radiusListFilter.size - 1).toFloat(),
                    steps = radiusListFilter.size - 2
                )
            }
        }
    }
}
