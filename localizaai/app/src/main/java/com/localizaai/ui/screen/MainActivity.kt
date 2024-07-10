package com.localizaai.ui.screen

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.localizaai.Model.WeatherRequest
import com.localizaai.ui.ViewModel.AboutScreenViewModel
import com.localizaai.ui.ViewModel.LoginScreenViewModel
import com.localizaai.ui.ViewModel.MainActivityViewModel
import com.localizaai.ui.ViewModel.MenuScreenViewModel
import com.localizaai.ui.ViewModel.RegisterScreenViewModel
import com.localizaai.ui.ViewModel.SettingsScreenViewModel
import com.localizaai.ui.factory.LoginScreenViewModelFactory
import com.localizaai.ui.factory.MainActivityViewModelFactory
import com.localizaai.ui.factory.RegisterScreenViewModelFactory
import com.localizaai.ui.factory.SettingsScreenViewModelFactory
import com.localizaai.ui.screen.ui.theme.localizaaiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.localizaai.R
import kotlinx.coroutines.runBlocking
import java.sql.Timestamp
import java.util.Calendar


@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationLatLng: Location? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MapsInitializer.initialize(applicationContext)
            val context = applicationContext
            val viewModel: MainActivityViewModel = viewModel(factory = MainActivityViewModelFactory(context))
            val registerViewModel: RegisterScreenViewModel = viewModel(factory = RegisterScreenViewModelFactory(context))
            val loginViewModel: LoginScreenViewModel = viewModel(factory = LoginScreenViewModelFactory(context))
            val menuViewModel: MenuScreenViewModel = viewModel()
            val settingsViewModel: SettingsScreenViewModel = viewModel(factory = SettingsScreenViewModelFactory(context))
            val aboutViewModel: AboutScreenViewModel = viewModel()
            val navController = rememberNavController()
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            viewModel.loadLanguageState(this)
            viewModel.loadThemeState(this)
            val themeMode = viewModel.themeMode.value

            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    MainScreen(viewModel = viewModel, navController = navController)
                }
                composable("register") {
                    RegisterScreen(registerViewModel, navController, themeMode, context)
                }
                composable("login") {
                    LoginScreen(loginViewModel, navController, themeMode, context)
                }
                composable("menu") {
                    MenuScreen(menuViewModel, navController, themeMode ,context, fusedLocationProviderClient)
                }
                composable("settings") {
                    SettingsScreen(settingsViewModel, navController, themeMode, context)
                }
                composable("about") {
                    AboutScreen(aboutViewModel, navController, themeMode, context)
                }
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequestLocationPermissions(context: Context, viewModel: MainActivityViewModel, language: String) {
        val permissionState = rememberMultiplePermissionsState(
            permissions = listOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        LaunchedEffect(permissionState) {
            if (permissionState.allPermissionsGranted) {
                prepareDataForApi(context,viewModel, language)
            }else{
                permissionState.launchMultiplePermissionRequest()
            }
        }
    }

    private fun prepareDataForApi(context: Context, viewModel: MainActivityViewModel, language: String) {
        viewModel.startLocationUpdates(fusedLocationProviderClient, context) { location ->
            locationLatLng = location
            val cityName = viewModel.getCityNameFromLocation(context, locationLatLng!!).toString()
            val days = 3
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val lang = language

            val weatherData = WeatherRequest(cityName, days, hour, lang)

            viewModel.loadWeatherProps(context, weatherData)
            viewModel.loadTrafficProps(context, location)
            viewModel.loadEventsProps(context, lang)
        }
    }


    @Composable
    fun MainScreen(
        viewModel: MainActivityViewModel,
        navController: NavController,
    ) {
        val context = this@MainActivity
        val language = viewModel.language.value
        val themeMode = viewModel.themeMode.value

        RequestLocationPermissions(context, viewModel, language)


        localizaaiTheme(darkTheme = themeMode) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MainContent(
                    viewModel = viewModel,
                    navController = navController,
                    themeMode = themeMode,
                    language = language,
                    context = context
                )
            }
        }
    }

    @Composable
    fun MainContent(
        viewModel: MainActivityViewModel,
        navController: NavController,
        themeMode: Boolean,
        language: String,
        context: Context
    ) {
        viewModel.verifyToken(context)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 42.sp,
                style = MaterialTheme.typography.titleLarge
            )
            val logoDrawable = if (themeMode) R.drawable.logo_white else R.drawable.logo_black
            Image(
                painter = painterResource(id = logoDrawable),
                contentDescription = "Logo",
                modifier = Modifier.size(300.dp)
            )
            if(viewModel.hasToken){
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = viewModel.weatherResponse?.current?.temp_c?.let {"$it°C" } ?: "",
                        fontSize = 26.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.width(36.dp))
                    val icon = remember { mutableStateOf(R.drawable.ic_car_unknown) }
                    LaunchedEffect(viewModel.trafficResponse) {
                        if (viewModel.trafficResponse != null) {
                            val iconResource = viewModel.checkTrafficStatus()
                            icon.value = iconResource
                        }
                    }
                    Icon(
                        painter = painterResource(id = icon.value),
                        contentDescription = "",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Unspecified
                    )

                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        if (viewModel.hasToken) {
                            navController.navigate("menu")
                        } else {
                            navController.navigate("register")
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.start),
                        fontSize = 24.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { viewModel.toggleThemeMode(context) },
                    modifier = Modifier.padding(16.dp)
                ) {
                    val icon = if (themeMode) Icons.Filled.WbSunny else Icons.Filled.Nightlight
                    Icon(
                        icon,
                        contentDescription = "Modo ${if (themeMode) "Claro" else "Escuro"}",
                        modifier = Modifier.size(28.dp)
                    )
                }
                Button(
                    onClick = { viewModel.toggleLanguage(context) },
                    modifier = Modifier.padding(16.dp)
                ) {
                    val icon = when (language) {
                        "en" -> R.drawable.brazil_flag
                        else -> R.drawable.usa_flag
                    }

                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "Linguagem ${if (language == "pt") "Português" else "Inglês"}",
                        modifier = Modifier.size(28.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

