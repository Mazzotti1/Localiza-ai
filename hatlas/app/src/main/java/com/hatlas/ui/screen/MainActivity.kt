package com.hatlas.ui.screen

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.hatlas.ui.ViewModel.AboutScreenViewModel
import com.hatlas.ui.ViewModel.LoginScreenViewModel
import com.hatlas.ui.ViewModel.MainActivityViewModel
import com.hatlas.ui.ViewModel.MenuScreenViewModel
import com.hatlas.ui.ViewModel.RegisterScreenViewModel
import com.hatlas.ui.ViewModel.SettingsScreenViewModel
import com.hatlas.ui.factory.LoginScreenViewModelFactory
import com.hatlas.ui.factory.MainActivityViewModelFactory
import com.hatlas.ui.factory.RegisterScreenViewModelFactory
import com.hatlas.ui.factory.SettingsScreenViewModelFactory
import com.hatlas.ui.screen.ui.theme.localizaaiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.MapsInitializer

import com.hatlas.R
import com.hatlas.ui.factory.AboutScreenViewModelFactory
import com.hatlas.ui.factory.MenuScreenViewModelFactory
import java.util.Locale


@OptIn(ExperimentalPermissionsApi::class)
class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationLatLng: Location? = null
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locale = Locale("pt", "BR")
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        createConfigurationContext(config)

        setContent {
            MapsInitializer.initialize(applicationContext)
            val context = applicationContext
            val viewModel: MainActivityViewModel = viewModel(factory = MainActivityViewModelFactory(context))
            val registerViewModel: RegisterScreenViewModel = viewModel(factory = RegisterScreenViewModelFactory(context))
            val loginViewModel: LoginScreenViewModel = viewModel(factory = LoginScreenViewModelFactory(context))
            val menuViewModel: MenuScreenViewModel = viewModel(factory = MenuScreenViewModelFactory(context))
            val settingsViewModel: SettingsScreenViewModel = viewModel(factory = SettingsScreenViewModelFactory(context))
            val aboutViewModel: AboutScreenViewModel = viewModel(factory = AboutScreenViewModelFactory(context))
            val navController = rememberNavController()
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            viewModel.loadLanguageState(this)
            viewModel.loadThemeState(this)
            val themeMode = viewModel.themeMode.value
            val showHeatMap = remember { mutableStateOf(false) }
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
                    SettingsScreen(settingsViewModel, navController, themeMode, context, fusedLocationProviderClient)
                }
                composable("about") {
                    AboutScreen(aboutViewModel, navController, themeMode, context, fusedLocationProviderClient)
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
            if (!permissionState.allPermissionsGranted) {
                permissionState.launchMultiplePermissionRequest()
                Log.d("Vish", "Nothing here hehe")
            }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 52.sp,
                style = MaterialTheme.typography.titleLarge
            )
            val logoDrawable = R.drawable.logo
            Image(
                painter = painterResource(id = logoDrawable),
                contentDescription = "Logo",
                modifier = Modifier.size(220.dp)
            )
            Spacer(modifier = Modifier.height(90.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
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
                        .width(200.dp),
                    shape = RoundedCornerShape(8.dp)
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
                    modifier = Modifier.padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    val icon = if (themeMode) Icons.Filled.WbSunny else Icons.Filled.Nightlight
                    Icon(
                        icon,
                        contentDescription = "Modo ${if (themeMode) "Claro" else "Escuro"}",
                        modifier = Modifier.size(42.dp),
                    )
                }
                Button(
                    onClick = { viewModel.toggleLanguage(context) },
                    modifier = Modifier.padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    val icon = when (language) {
                        "en" -> R.drawable.brazil_flag
                        else -> R.drawable.usa_flag
                    }

                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = "Linguagem ${if (language == "pt") "Português" else "Inglês"}",
                        modifier = Modifier.size(42.dp),
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

