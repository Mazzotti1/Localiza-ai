package com.localizaai.ui.screen

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.localizaai.Model.WeatherRequest
import com.localizaai.R
import com.localizaai.ui.ViewModel.AboutScreenViewModel
import com.localizaai.ui.factory.AboutScreenViewModelFactory
import com.localizaai.ui.screen.ui.theme.localizaaiTheme
import com.localizaai.ui.util.CustomTopBar
import com.localizaai.ui.util.NavigationBar
import java.util.Calendar


class AboutActivity : ComponentActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationLatLng: Location? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = applicationContext
            val viewModel: AboutScreenViewModel =
                viewModel(factory = AboutScreenViewModelFactory(context))
            val navController = rememberNavController()
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            viewModel.loadThemeState(this)

            val themeMode = viewModel.themeMode.value
            AboutScreen(viewModel, navController, themeMode, context, fusedLocationProviderClient)

        }
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AboutScreen(
    viewModel: AboutScreenViewModel,
    navController: NavController,
    themeMode: Boolean,
    context: Context,
    fusedLocationProviderClient : FusedLocationProviderClient,
) {
    var locationLatLng: Location? = null
    val language = viewModel.language.value

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
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopBar(viewModel,navController)
                },
                bottomBar = {
                    NavigationBar(navController, context)
                }
            ) {
                AboutContent(
                    viewModel = viewModel,
                    navController = navController,
                    themeMode = themeMode,
                    context = context,

                    )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(viewModel: AboutScreenViewModel, navController : NavController) {
    val trafficData = viewModel.trafficResponse
    val weatherData = viewModel.weatherResponse
    CustomTopBar(
        navController = navController,
        viewModel = viewModel,
        title = stringResource(id = R.string.about_title),
        temperature = weatherData,
        traffic = trafficData,
        onBackClick = {
            viewModel.onBackPressed(navController)
        }
    )
}

data class SocialButton(
    val url: String,
    val iconWhite: Int,
    val iconBlack: Int,
    val contentDescription: String
)

val buttons = listOf(
    SocialButton("https://www.linkedin.com/in/gabriel-mazzotti/", R.drawable.linkedin_white, R.drawable.linkedin_black, "Linkedin"),
    SocialButton("https://github.com/Mazzotti1", R.drawable.github_white, R.drawable.github_black, "Github"),
    SocialButton("https://portifolio.gabrielmazzotti.com.br/", R.drawable.person_white, R.drawable.person_black, "Portfolio")
)

@Composable
fun SocialButtonList(themeMode: Boolean, buttons: List<SocialButton>, viewModel : AboutScreenViewModel, context : Context) {

        for (button in buttons) {
            IconButton(
                onClick = { viewModel.onSocialPressed(button.url, context) },
                modifier = Modifier.padding(16.dp)
            ) {
                val iconResource = if (themeMode) button.iconWhite else button.iconBlack
                Icon(
                    painter = painterResource(id = iconResource),
                    contentDescription = button.contentDescription,
                    modifier = Modifier.size(172.dp),
                    tint = Color.Unspecified
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
    }
}

@Composable
fun AboutContent(
    viewModel: AboutScreenViewModel,
    navController: NavController,
    themeMode: Boolean,
    context: Context
) {
    val focusManager = LocalFocusManager.current
    viewModel.getTokenProps(context)

    Column (
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
        ){
            val logoDrawable = R.drawable.logo
            Image(
                painter = painterResource(id = logoDrawable),
                contentDescription = "Logo",
                modifier = Modifier.size(190.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 32.sp,
                style = MaterialTheme.typography.titleLarge,
                modifier =
                    Modifier.padding(10.dp),

            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.about_text),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.about_by),
                fontSize = 18.sp,
                modifier = Modifier.padding(15.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SocialButtonList(themeMode, buttons, viewModel, context)
        }
    }
}