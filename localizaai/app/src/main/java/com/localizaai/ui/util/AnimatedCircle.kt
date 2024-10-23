package com.localizaai.ui.util

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle

@Composable
fun AnimatedCircle(position: LatLng, maxRepeats: Int) {
    var currentRepeat by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(true) }
    val radius = remember { Animatable(10f) }

    LaunchedEffect(isAnimating) {
        while (isAnimating && currentRepeat < maxRepeats) {
            radius.animateTo(
                targetValue = 80f,
                animationSpec = tween(1500)
            )
            radius.animateTo(
                targetValue = 10f,
                animationSpec = tween(1500)
            )
            currentRepeat++
        }
        isAnimating = false // Parar a animação quando o número máximo de repetições for atingido
    }

    if (isAnimating) {
        Circle(
            center = position,
            radius = radius.value.toDouble(),
            fillColor = Color(0x5500FF00),
            strokeColor = Color(0xFF00FF00),
            strokeWidth = 2f
        )
    }
}


