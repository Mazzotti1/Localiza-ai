package com.ecoheat.ui.ViewModel

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
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.ecoheat.ui.screen.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.ecoheat.Model.Login
import com.ecoheat.Model.WeatherRequest
import com.ecoheat.R
import com.ecoheat.data.repository.EventsRepository
import com.ecoheat.data.repository.PlacesRepository
import com.ecoheat.data.repository.TrafficRepository
import com.ecoheat.data.repository.WeatherRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
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
    private val placesRepository = PlacesRepository(context)

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
            val savedLanguage = sharedPreferences.getString("language", "en") ?: "en"
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
                println("Resultado da consulta do tempo é: $result")
                Log.d("WeatherApi", "Resultado da consulta do tempo é: ${result.toString()}")
            }.onFailure { exception ->
                println("Error: ${exception.message}")
                Log.d("WeatherApi", "Error: ${exception.message}")
            }
        }
    }

    fun loadTrafficProps(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val lat = -29.916772
        val lon = -51.166009

        viewModelScope.launch(Dispatchers.IO) {
            val result = trafficRepository.fetchTrafficData(lat, lon)

            result.onSuccess { responseBody ->
                println("Resultado da consulta do Trafego é: $result")
                Log.d("TrafficApi", "Resultado da consulta do Trafego é: ${result.toString()}")
            }.onFailure { exception ->
                println("Error: ${exception.message}")
                Log.d("TrafficApi", "Error: ${exception.message}")
            }
        }
    }

    fun loadEventsProps(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val localRequest = "pt-br.brazilian"

        viewModelScope.launch(Dispatchers.IO) {
            val result = eventsRepository.fetchEventsData(localRequest)

            result.onSuccess { responseBody ->
                println("Resultado da consulta do Trafego é: $result")
                Log.d("EventsApi", "Resultado da consulta do Trafego é: ${result.toString()}")
            }.onFailure { exception ->
                println("Error: ${exception.message}")
                Log.d("EventsApi", "Error: ${exception.message}")
            }
        }
    }

    fun loadPlacesProps(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val lat = "-29.917928"
        val long = "-51.160637"
        val radius = "350"
        val sort = "POPULARITY"

        viewModelScope.launch(Dispatchers.IO) {
            val result = placesRepository.fetchPlacesData(lat,long,radius,sort)

            result.onSuccess { responseBody ->
                println("Resultado da consulta do Trafego é: $result")
                Log.d("PlacesApi", "Resultado da consulta do Places é: ${result.toString()}")
            }.onFailure { exception ->
                println("Error: ${exception.message}")
                Log.d("PlacesApi", "Error: ${exception.message}")
            }
        }
    }

    fun loadSpecificPlacesProps(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val id = "5da27c64227dcb000880cc7c"

        viewModelScope.launch(Dispatchers.IO) {
            val result = placesRepository.fetchSpecificPlacesData(id)

            result.onSuccess { responseBody ->
                println("Resultado da consulta do Trafego é: $result")
                Log.d("PlacesApi", "Resultado da consulta do Places Specific é: ${result.toString()}")
            }.onFailure { exception ->
                println("Error: ${exception.message}")
                Log.d("PlacesApi", "Error: ${exception.message}")
            }
        }
    }

    fun loadPlacesByNameProps(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val lat = "-29.917928"
        val long = "-51.160637"
        val name = "Taishi"

        viewModelScope.launch(Dispatchers.IO) {
            val result = placesRepository.fetchPlaceByNameData(lat,long,name)

            result.onSuccess { responseBody ->
                println("Resultado da consulta do Trafego é: $result")
                Log.d("PlacesApi", "Resultado da consulta do Places by Name é: ${result.toString()}")
            }.onFailure { exception ->
                println("Error: ${exception.message}")
                Log.d("PlacesApi", "Error: ${exception.message}")
            }
        }
    }

    fun loadPlacesTipsProps(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val id = "5da27c64227dcb000880cc7c"

        viewModelScope.launch(Dispatchers.IO) {
            val result = placesRepository.fetchPlacesTipsData(id)

            result.onSuccess { responseBody ->
                println("Resultado da consulta do Trafego é: $result")
                Log.d("PlacesApi", "Resultado da consulta do Places Tips é: ${result.toString()}")
            }.onFailure { exception ->
                println("Error: ${exception.message}")
                Log.d("PlacesApi", "Error: ${exception.message}")
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

}
