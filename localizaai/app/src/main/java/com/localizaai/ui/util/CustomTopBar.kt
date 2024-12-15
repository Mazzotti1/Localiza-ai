package com.localizaai.ui.util

import android.content.Context
import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.localizaai.Model.TrafficResponse
import com.localizaai.Model.WeatherRequest
import com.localizaai.Model.WeatherResponse
import com.localizaai.R
import com.localizaai.ui.ViewModel.MainActivityViewModel
import com.localizaai.ui.ViewModel.SettingsScreenViewModel
import java.util.Calendar

@Composable
fun CustomTopBar(
    navController: NavController,
    viewModel: Any,
    title: String,
    temperature: WeatherResponse?,
    traffic: TrafficResponse?,
    onBackClick : () -> Unit
) {
    val icon =
    Surface(
        color = MaterialTheme.colorScheme.tertiary,
        shadowElevation = 4.dp
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,

            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                if (temperature != null) {
                    Text(
                        text = "${temperature.current.temp_c}°C",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                if (traffic != null) {
                    Icon(
                        painter = painterResource(id = checkTrafficStatus(traffic)),
                        contentDescription = "Action Icon",
                        Modifier.size(35.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

fun checkTrafficStatus(trafficResponse : TrafficResponse): Int {
    val currentSpeed = trafficResponse?.currentSpeed
    val freeFlowSpeed = trafficResponse?.freeFlowSpeed
    val currentTravelTime = trafficResponse?.currentTravelTime
    val freeFlowTravelTime = trafficResponse?.freeFlowTravelTime
    val roadClosure = trafficResponse?.roadClosure

    return when {
        roadClosure == true -> R.drawable.ic_car_red

        currentSpeed != null && freeFlowSpeed != null && currentSpeed < freeFlowSpeed * 0.5 -> R.drawable.ic_car_red

        currentSpeed != null && freeFlowSpeed != null && currentSpeed < freeFlowSpeed * 0.75 -> R.drawable.ic_car_yellow

        currentTravelTime != null && freeFlowTravelTime != null && currentTravelTime > freeFlowTravelTime * 1.5 -> R.drawable.ic_car_red

        currentTravelTime != null && freeFlowTravelTime != null && currentTravelTime > freeFlowTravelTime * 1.25 -> R.drawable.ic_car_yellow

        currentSpeed != null && freeFlowSpeed != null -> R.drawable.ic_car_green

        else -> R.drawable.ic_car_unknown
    }

}
