package com.hatlas.ui.util
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun NavigationBar(
    navController: NavController,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.Black)
            ,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = { navController.navigate("main") {
                    popUpTo(navController.graph.startDestinationId)
                }
            }) {
                Icon(imageVector = Icons.Filled.Home, contentDescription = "Home", tint= Color.White)
            }
            IconButton(onClick = { navController.navigate("settings")}) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings", tint= Color.White)
            }
        }
    }
}
