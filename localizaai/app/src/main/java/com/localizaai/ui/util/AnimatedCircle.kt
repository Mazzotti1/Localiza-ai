package com.localizaai.ui.util

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle

@Composable
fun AnimatedCircle(position: LatLng) {
    val infiniteTransition = rememberInfiniteTransition()
    val radius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Circle(
        center = position,
        radius = radius.toDouble(),
        fillColor = Color(0x5500FF00),
        strokeColor = Color(0xFF00FF00),
        strokeWidth = 2f
    )
}