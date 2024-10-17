package com.localizaai.ui.screen

import PlaceModal
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.localizaai.ui.ViewModel.MenuScreenViewModel
import com.localizaai.ui.ViewModel.SettingsScreenViewModel
import com.localizaai.ui.screen.ui.theme.localizaaiTheme
import com.localizaai.ui.util.NavigationBar
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_TERRAIN
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.localizaai.Model.SpecificPlaceResponse
import com.localizaai.ui.factory.MenuScreenViewModelFactory
import com.localizaai.ui.util.AnimatedCircle
import com.localizaai.ui.util.FilterButton
import com.localizaai.ui.util.HeatMapButton
import com.localizaai.ui.util.MapViewButton
import com.localizaai.ui.util.ResetButton
import com.localizaai.ui.util.SearchBarMain
import com.localizaai.ui.util.SearchResultList
import com.localizaai.ui.util.performSearch
import android.graphics.Color as newColor


class MenuActivity : ComponentActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = applicationContext
            val viewModel: MenuScreenViewModel = viewModel(factory = MenuScreenViewModelFactory(context))
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermissions(
    context: Context,
    viewModel: MenuScreenViewModel,
    fusedLocationProviderClient: FusedLocationProviderClient,
    ) {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    DisposableEffect(permissionState) {
        if (permissionState.allPermissionsGranted) {
            if(!viewModel.shouldStopUpdateUserLocation.value){
                prepareDataForApi(context, viewModel, fusedLocationProviderClient)
            }
        }else{
            permissionState.launchMultiplePermissionRequest()
        }
        onDispose {

        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun prepareDataForApi(
    context: Context,
    viewModel: MenuScreenViewModel,
    fusedLocationProviderClient: FusedLocationProviderClient,
) {
    viewModel.startPlacesLocationUpdates(fusedLocationProviderClient, context) { location ->
        viewModel.loadPlacesAround(context, location)

        val latLng = LatLng(location.latitude, location.longitude)
        viewModel.getWeatherData(latLng)
        viewModel.getTrafficData(latLng)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MenuScreen(
    viewModel: MenuScreenViewModel,
    navController: NavController,
    themeMode: Boolean,
    context: Context,
    fusedLocationProviderClient: FusedLocationProviderClient
) {
    localizaaiTheme(darkTheme = themeMode) {
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
                    fusedLocationProviderClient = fusedLocationProviderClient
                )
            }
        }
    }
    RequestLocationPermissions(
        context = context,
        viewModel = viewModel,
        fusedLocationProviderClient = fusedLocationProviderClient,
    )
}


@RequiresApi(Build.VERSION_CODES.O)
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


@RequiresApi(Build.VERSION_CODES.O)
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

    var showPlaceInfoDialog by remember { mutableStateOf(false) }
    val autocompletePlaces by viewModel.autocompletePlaces
    var showSearchListItens by viewModel.showSearchListItens
    var isFocused by remember { mutableStateOf(false) }
    val shouldMoveCamera by viewModel.shouldMoveCamera
    val shouldMoveCameraToNewDestiny by viewModel.shouldMoveCameraToNewDestiny
    val newLatLng by viewModel.newLatLng
    var selectedPlace by viewModel.selectedPlace

    val clickedLatLng by viewModel.clickedLatLng.observeAsState()
    val isSlideDistanceVisible = remember { mutableStateOf(false) }
    val shouldShowHeatMap by viewModel.showHeatMap.collectAsState()
    val shouldChangeMapView by viewModel.changeMapView.collectAsState()

    val properties = if (shouldChangeMapView) {
        MapProperties(mapType = MapType.TERRAIN)
    } else {
        MapProperties(mapType = MapType.HYBRID)
    }

    LaunchedEffect(viewModel.isDialogPlaceOpen.value) {
        showPlaceInfoDialog = viewModel.isDialogPlaceOpen.value
    }

    DisposableEffect(permissionState) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        } else {
            viewModel.startLocationUpdates(fusedLocationProviderClient, context) { location ->

                val currentLatLng = LatLng(location.latitude, location.longitude)
                markerState.position = currentLatLng
                if (shouldMoveCamera) {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
                    viewModel.shouldMoveCamera.value = false
                }

                if (shouldMoveCameraToNewDestiny) {
                    newLatLng?.let {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 16f))
                    }
                    viewModel.shouldMoveCameraToNewDestiny.value = false
                }
            }
        }
        onDispose {
            viewModel.stopLocationUpdates(fusedLocationProviderClient)
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

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    val customIconPlayer by remember {
        val iconMaker = com.localizaai.R.drawable.pointer_small
        val customIconBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, iconMaker)
        mutableStateOf(BitmapDescriptorFactory.fromBitmap(customIconBitmap))
    }

    val customIconPlace by remember {
        val iconMaker = com.localizaai.R.drawable.pin_marker_medium
        val customIconBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, iconMaker)
        mutableStateOf(BitmapDescriptorFactory.fromBitmap(customIconBitmap))
    }

    val customMatchedIconPlace by remember {
        val iconMaker = com.localizaai.R.drawable.pin_marker_matched_medium
        val customIconBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, iconMaker)
        mutableStateOf(BitmapDescriptorFactory.fromBitmap(customIconBitmap))
    }

    

    var heatmapTileProvider by remember {
        mutableStateOf<HeatmapTileProvider?>(null)
    }

    val heatmapData by remember { derivedStateOf { viewModel.getHeatmapData() } }

    LaunchedEffect(heatmapData) {
        heatmapTileProvider = HeatmapTileProvider.Builder()
            .weightedData(heatmapData)
            .radius(50)
            .opacity(0.4)
            .build()


    }


    val specificPlaceResponse = viewModel.specificPlaceList
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = properties,
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->

                if(isSlideDistanceVisible.value == false && viewModel.isDialogPlaceOpen.value == false && isFocused == false){
                    viewModel.updateClickedLatLng(latLng.latitude, latLng.longitude)
                }

                viewModel.isDialogPlaceOpen.value = false
                isSlideDistanceVisible.value = false
                focusManager.clearFocus()
            },
            uiSettings = remember {
                MapUiSettings(
                    zoomControlsEnabled =  false,
                )
            }
        ) {

            Marker(
                state = markerState,
                icon = customIconPlayer,
                anchor = Offset(0.5f, 0.5f),
                flat = true,
                rotation = rotation.value
            )

            clickedLatLng?.let {
                AnimatedCircle(position = LatLng(it.first, it.second))
            }

            if (shouldShowHeatMap && heatmapTileProvider != null) {
                TileOverlay(
                    tileProvider = heatmapTileProvider!!
                )
            } else {
                specificPlaceResponse.forEach { place ->
                    val placeLatLng = place.geocodes?.main?.latitude?.let {
                        place.geocodes.main.longitude?.let { it1 ->
                            LatLng(it, it1)
                        }
                    }


                    val icon: BitmapDescriptor = if (selectedPlace == place.name) {
                        customMatchedIconPlace
                    } else {
                        customIconPlace
                    }

                    placeLatLng?.let { MarkerState(position = it) }?.let {
                        Marker(
                            state = it,
                            icon = icon,
                            anchor = Offset(0.5f, 0.5f),
                            onClick = {
                                viewModel.getAllPlaceInfo(place.name, place.geocodes.main.latitude.toString(), place.geocodes.main.longitude.toString())
                                true
                            }
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
        ) {
            Spacer(modifier = Modifier.height(64.dp))
            SearchBarMain(
                viewModel,
                onSearch = { query ->
                    performSearch(query, viewModel)
                },
                isFocused = isFocused,
                onFocusChanged = {
                    isFocused = it
                }
            )
            if (autocompletePlaces != null && showSearchListItens) {
                Spacer(modifier = Modifier.height(2.dp))
                SearchResultList(context, autocompletePlaces, viewModel)
            }
        }
        if (showPlaceInfoDialog) {
            viewModel.infosPlaceResponse?.let { PlaceModal(onDismiss = { viewModel.isDialogPlaceOpen.value = false }, placeInfo = it) }
        }
        HeatMapButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 170.dp),
            onClick = { viewModel.onHeatMapChange()}
        )
        MapViewButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 250.dp),
            onClick = { viewModel.onMapViewChange()}
        )
        ResetButton(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 78.dp)

        ) {
            val currentLatLng = viewModel.onResetButtonClick()
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
        }
        FilterButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp)
                .padding(bottom = 78.dp),
            onClick = { isSlideDistanceVisible.value = !isSlideDistanceVisible.value },
            isVisible = isSlideDistanceVisible.value,
            viewModel
        )
    }

}



