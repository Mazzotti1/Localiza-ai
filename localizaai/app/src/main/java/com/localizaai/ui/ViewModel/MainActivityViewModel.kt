package com.localizaai.ui.ViewModel

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Geocoder
import android.location.Location
import android.media.session.MediaSession.Token
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.localizaai.ui.screen.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.localizaai.Model.Login
import com.localizaai.Model.TrafficResponse
import com.localizaai.Model.WeatherRequest
import com.localizaai.Model.WeatherResponse
import com.localizaai.R
import com.localizaai.data.repository.EventsRepository
import com.localizaai.data.repository.PlacesRepository
import com.localizaai.data.repository.TrafficRepository
import com.localizaai.data.repository.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import java.util.jar.Manifest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivityViewModel(private val context: Context) : ViewModel() {
    val themeMode = mutableStateOf(true)
    val language = mutableStateOf("pt")
    private val languages = mutableListOf("en", "pt")
    var hasToken: Boolean = false

    private val weatherRepository = WeatherRepository(context)
    private val trafficRepository = TrafficRepository(context)
    private val eventsRepository = EventsRepository(context)


    var weatherResponse by mutableStateOf<WeatherResponse?>(null)
    var trafficResponse by mutableStateOf<TrafficResponse?>(null)

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun toggleThemeMode(context: Context) {
        themeMode.value = !themeMode.value
        saveThemeState(context)
    }

    private fun saveThemeState(context: Context) {
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("mode", themeMode.value).apply()
    }

    fun loadThemeState(context: Context) {
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        themeMode.value = sharedPreferences.getBoolean("mode", true)
    }

    fun toggleLanguage(context: Context) {
        val currentIndex = languages.indexOf(language.value)
        val nextIndex = (currentIndex + 1) % languages.size
        language.value = languages[nextIndex]

        saveLanguageState(context)

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun saveLanguageState(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("language", language.value).apply()
    }

    fun loadLanguageState(context: Context) {
            val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val savedLanguage = sharedPreferences.getString("language", "pt") ?: "pt"
            language.value = savedLanguage

            val locale = Locale(savedLanguage)
            Locale.setDefault(locale)
            val configuration = Configuration()
            configuration.setLocale(locale)
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }

    fun verifyToken(context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPreferences =
                context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("jwtToken", "")
            hasToken = !token.isNullOrBlank()
        }
    }

    fun loadWeatherProps(context: Context, weatherData : WeatherRequest){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val q = weatherData.cityName
        val hour = weatherData.hour
        val days = weatherData.days
        val lang = weatherData.lang

        viewModelScope.launch(Dispatchers.IO) {
            val result = weatherRepository.fetchWeatherData(q, hour, days, lang)

            result.onSuccess { responseBody ->
                val weatherJson = responseBody.toString()
                val gson = Gson()
                weatherResponse = gson.fromJson(weatherJson, WeatherResponse::class.java)

                Log.d("WeatherApi", "Resultado da consulta do tempo é: ${result.toString()}")
            }.onFailure { exception ->
                Log.d("WeatherApi", "Error: ${exception.message}")
            }
        }
    }

    fun loadTrafficProps(context: Context, location: Location){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val lat = location.latitude
        val lon = location.longitude

        viewModelScope.launch(Dispatchers.IO) {
            val result = trafficRepository.fetchTrafficData(lat, lon)

            result.onSuccess { responseBody ->

                val trafficJson = responseBody.toString()
                val gson = Gson()
                trafficResponse = gson.fromJson(trafficJson, TrafficResponse::class.java)

                Log.d("TrafficApi", "Resultado da consulta do Trafego é: ${result.toString()}")
            }.onFailure { exception ->
                Log.d("TrafficApi", "Error: ${exception.message}")
            }
        }
    }

    fun loadEventsProps(context: Context, lang : String){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val localRequest : String
        when (lang) {
            "pt" -> {
                localRequest = "pt-br.brazilian"
            }
            "en" -> {
                localRequest = "en.usa"
            }
            else -> {
                localRequest = "pt-br.brazilian"
            }
        }


        viewModelScope.launch(Dispatchers.IO) {
            val result = eventsRepository.fetchEventsData(localRequest)

            result.onSuccess { responseBody ->
                Log.d("EventsApi", "Resultado da consulta de eventos é: ${result.toString()}")
            }.onFailure { exception ->
                Log.d("EventsApi", "Error: ${exception.message}")
            }
        }
    }

    fun startLocationUpdates(
        fusedLocationProviderClient: FusedLocationProviderClient,
        context: Context,
        onLocationUpdate: (Location) -> Unit
    ) {
        val locationRequest = LocationRequest.create().apply {
            interval = 1000 * 60 * 60
            fastestInterval = 1000 * 60 * 60
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    onLocationUpdate(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }


    fun getCityNameFromLocation(context: Context, location: Location): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                addresses[0]?.locality
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun checkTrafficStatus(): Int {
        val currentSpeed = trafficResponse?.currentSpeed
        val freeFlowSpeed = trafficResponse?.freeFlowSpeed
        val currentTravelTime = trafficResponse?.currentTravelTime
        val freeFlowTravelTime = trafficResponse?.freeFlowTravelTime
        val roadClosure = trafficResponse?.roadClosure

        return when {
            roadClosure == true -> R.drawable.ic_car_red // Ícone vermelho para fechamento de estrada

            currentSpeed != null && freeFlowSpeed != null && currentSpeed < freeFlowSpeed * 0.5 -> R.drawable.ic_car_red // Ícone vermelho para trânsito muito lento

            currentSpeed != null && freeFlowSpeed != null && currentSpeed < freeFlowSpeed * 0.75 -> R.drawable.ic_car_yellow // Ícone amarelo para trânsito moderadamente lento

            currentTravelTime != null && freeFlowTravelTime != null && currentTravelTime > freeFlowTravelTime * 1.5 -> R.drawable.ic_car_red // Ícone vermelho para tempo de viagem muito alto

            currentTravelTime != null && freeFlowTravelTime != null && currentTravelTime > freeFlowTravelTime * 1.25 -> R.drawable.ic_car_yellow // Ícone amarelo para tempo de viagem moderadamente alto

            currentSpeed != null && freeFlowSpeed != null -> R.drawable.ic_car_green // Ícone verde para trânsito bom

            else -> R.drawable.ic_car_unknown // Ícone desconhecido
        }

    }

}
