package com.hatlas.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.hatlas.Model.WeatherRequest
import com.hatlas.R
import com.hatlas.ui.ViewModel.AboutScreenViewModel
import com.hatlas.ui.ViewModel.SettingsScreenViewModel
import com.hatlas.ui.factory.AboutScreenViewModelFactory
import com.hatlas.ui.factory.SettingsScreenViewModelFactory
import com.hatlas.ui.screen.ui.theme.localizaaiTheme
import com.hatlas.ui.util.ConfirmationDialog
import com.hatlas.ui.util.CustomTopBar
import com.hatlas.ui.util.LoadingIndicator
import com.hatlas.ui.util.NavigationBar
import java.util.Calendar


class SettingsAcitivty : ComponentActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationLatLng: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = applicationContext
            val viewModel: SettingsScreenViewModel =
                viewModel(factory = SettingsScreenViewModelFactory(context))
            val aboutViewModel: AboutScreenViewModel =
                viewModel(factory = AboutScreenViewModelFactory(context))

            val navController = rememberNavController()
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            viewModel.loadLanguageState(this)
            viewModel.loadThemeState(this)

            val themeMode = viewModel.themeMode.value
            SettingsScreen(viewModel, navController, themeMode, context, fusedLocationProviderClient)

            NavHost(navController = navController, startDestination = "settings") {
                composable("settings") {
                    SettingsScreen(
                        viewModel = viewModel,
                        navController = navController,
                        themeMode,
                        context,
                        fusedLocationProviderClient
                    )
                }
                composable("about") {
                    AboutScreen(aboutViewModel, navController, themeMode, context, fusedLocationProviderClient)
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
    navController: NavController,
    themeMode: Boolean,
    context: Context,
    fusedLocationProviderClient : FusedLocationProviderClient
) {
    var locationLatLng: Location? = null

    val language = viewModel.language.value
    val themeMode = viewModel.themeMode.value

    LaunchedEffect(Unit) {
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
        }
    }


    localizaaiTheme(darkTheme = themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopBar(viewModel, navController)
                },
                bottomBar = {
                    NavigationBar(navController, context)
                }
            ) {
                SettingsContent(
                    viewModel = viewModel,
                    navController = navController,
                    themeMode = themeMode,
                    language = language,
                    context = context

                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: SettingsScreenViewModel, navController: NavController) {
    val trafficData = viewModel.trafficResponse
    val weatherData = viewModel.weatherResponse
    CustomTopBar(
        navController = navController,
        viewModel = viewModel,
        title = stringResource(id = R.string.settings_title),
        temperature = weatherData,
        traffic = trafficData,
        onBackClick = {
            viewModel.onBackPressed(navController)
        }
    )
}

@Composable
fun SettingsContent(
    viewModel: SettingsScreenViewModel,
    navController: NavController,
    themeMode: Boolean,
    language: String,
    context: Context
) {
    val focusManager = LocalFocusManager.current
    viewModel.getTokenProps(context)
    val hasToken = viewModel.hasToken.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { focusManager.clearFocus() },
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(85.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            val logoDrawable = R.drawable.logo
            Image(
                painter = painterResource(id = logoDrawable),
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { navController.navigate("about") },
                modifier = Modifier
                    .padding(8.dp)
                    .width(IntrinsicSize.Max)
            ) {
                Text(
                    text = stringResource(id = R.string.about_title),
                    fontSize = 29.sp,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        if (hasToken) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        viewModel.isDialogLogoutOpen.value = true
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = stringResource(id = R.string.logout),
                        fontSize = 29.sp,
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        if (hasToken) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { viewModel.isDialogDeleteOpen.value = true },
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_account),
                        fontSize = 29.sp,
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        if (hasToken) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = { viewModel.isDialogDesactivateOpen.value = true },
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max)
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_desactivate),
                        fontSize = 29.sp,
                        color = Color.Red,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
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
                    modifier = Modifier.size(42.dp)
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
            if (viewModel.isDialogLogoutOpen.value) {
                ConfirmationDialog(
                    onDismissRequest = { viewModel.isDialogLogoutOpen.value = false },
                    onConfirmation = { viewModel.logout(context) },
                    icon = Icons.Default.Warning,
                    title = stringResource(id = R.string.logout_title),
                    message = stringResource(id = R.string.logout_cofirm),
                    confirmButtonText = stringResource(id = R.string.logout_yes),
                    dismissButtonText = stringResource(id = R.string.logout_cancel)
                )
            }
            if (viewModel.isDialogDeleteOpen.value) {
                ConfirmationDialog(
                    onDismissRequest = { viewModel.isDialogDeleteOpen.value = false },
                    onConfirmation = { viewModel.deleteAccount(context) },
                    icon = Icons.Default.Warning,
                    title = stringResource(id = R.string.delete_title),
                    message = stringResource(id = R.string.delete_cofirm),
                    confirmButtonText = stringResource(id = R.string.delete_yes),
                    dismissButtonText = stringResource(id = R.string.delete_cancel)
                )
            }
            if (viewModel.isDialogDesactivateOpen.value) {
                ConfirmationDialog(
                    onDismissRequest = { viewModel.isDialogDesactivateOpen.value = false },
                    onConfirmation = { viewModel.desactivateAccount(context) },
                    icon = Icons.Default.Warning,
                    title = stringResource(id = R.string.desactive_title),
                    message = stringResource(id = R.string.desactive_cofirm),
                    confirmButtonText = stringResource(id = R.string.desactive_yes),
                    dismissButtonText = stringResource(id = R.string.desactive_cancel)
                )
            }
            if (viewModel.isLoading) {
                LoadingIndicator()
            }
        }
    }
}
