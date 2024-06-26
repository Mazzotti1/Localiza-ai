package com.ecoheat.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ecoheat.ui.ViewModel.MenuScreenViewModel
import com.ecoheat.ui.ViewModel.SettingsScreenViewModel
import com.ecoheat.ui.screen.ui.theme.EcoHeatTheme
import com.ecoheat.ui.util.NavigationBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.runBlocking
import java.util.jar.Manifest
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


class MenuActivity : ComponentActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MenuScreenViewModel = viewModel()
            val context = applicationContext
            val navController = rememberNavController()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            viewModel.loadThemeState(this)

            val settingsViewModel: SettingsScreenViewModel = viewModel()
            val themeMode = viewModel.themeMode.value

            NavHost(navController = navController, startDestination = "menu") {
                composable("menu") {
                    MenuScreen(
                        viewModel = viewModel,
                        navController = navController,
                        themeMode = themeMode,
                        context = context,
                        fusedLocationProviderClient = fusedLocationProviderClient
                    )
                }
                composable("settings") {
                    SettingsScreen(settingsViewModel, navController, themeMode, context)
                }
            }
        }
    }
}



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MenuScreen(
    viewModel: MenuScreenViewModel,
    navController: NavController,
    themeMode: Boolean,
    context: Context,
    fusedLocationProviderClient: FusedLocationProviderClient
) {
    EcoHeatTheme(darkTheme = themeMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopBar(viewModel, navController)
                },
                bottomBar = {
                    NavigationBar(navController, context)
                }
            ) {
                MenuContent(
                    viewModel = viewModel,
                    navController = navController,
                    context = context,
                    fusedLocationProviderClient = fusedLocationProviderClient,
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: MenuScreenViewModel, navController: NavController) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                "Menu",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { viewModel.onBackPressed(navController) }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MenuContent(
    viewModel: MenuScreenViewModel,
    navController: NavController,
    context: Context,
    fusedLocationProviderClient: FusedLocationProviderClient,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    viewModel.getTokenProps(context)
    val cameraPositionState = rememberCameraPositionState()
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val markerState = rememberMarkerState()
    val rotation = remember { mutableStateOf(0.0f) }
    LaunchedEffect(permissionState) {
        if (permissionState.allPermissionsGranted) {
            viewModel.startLocationUpdates(fusedLocationProviderClient, context) { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)
                markerState.position = currentLatLng
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }
        } else {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)

                val azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                rotation.value = azimuthInDegrees
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL)

    val iconMaker = com.ecoheat.R.drawable.pointer_small_white
    val customIconBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, iconMaker)
    val customIcon: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(customIconBitmap)

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = markerState,
            icon = customIcon,
            anchor = Offset(0.5f, 0.5f),
            flat = true,
            rotation = rotation.value
        )
    }
}

