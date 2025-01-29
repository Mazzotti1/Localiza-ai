package com.hatlas.ui.ViewModel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.gson.Gson
import com.hatlas.Model.TrafficResponse
import com.hatlas.Model.WeatherRequest
import com.hatlas.Model.WeatherResponse
import com.hatlas.data.repository.DeleteRepository
import com.hatlas.data.repository.DesactivateRepository
import com.hatlas.data.repository.TrafficRepository
import com.hatlas.data.repository.WeatherRepository
import com.hatlas.ui.screen.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SettingsScreenViewModel(private val context: Context) : ViewModel() {
    val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    val themeMode = mutableStateOf(sharedPreferences.getBoolean("mode", true))
    var username by mutableStateOf("")
    var roleName by mutableStateOf("")
    val language = mutableStateOf(sharedPreferences.getString("language", "pt") ?: "pt")
    private val languages = mutableListOf("en", "pt")
    var hasToken = mutableStateOf(false)

    var isDialogLogoutOpen = mutableStateOf(false)
    var isDialogDeleteOpen = mutableStateOf(false)
    var isDialogDesactivateOpen = mutableStateOf(false)

    var isLoading by mutableStateOf(false)
    private val deleteRepository = DeleteRepository(context)
    private val desactivateRepository = DesactivateRepository(context)

    val deleteStatus = MutableLiveData<String?>()
    private var token = ""

    private val weatherRepository = WeatherRepository(context)
    private val trafficRepository = TrafficRepository(context)

    var weatherResponse by mutableStateOf<WeatherResponse?>(null)
    var trafficResponse by mutableStateOf<TrafficResponse?>(null)

    fun loadThemeState(context: Context) {
            val sharedPreferences =
                context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            themeMode.value = sharedPreferences.getBoolean("mode", true)
    }

    fun toggleThemeMode(context: Context) {
        themeMode.value = !themeMode.value
        saveThemeState(context)
    }

    private fun saveThemeState(context: Context) {
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("mode", themeMode.value).apply()
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun getTokenProps(context: Context) {
            val sharedPreferences =
                context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("jwtToken", "")

            if (!token.isNullOrBlank()) {
                try {
                    val jwt = JWT.decode(token)

                    username = jwt.getClaim("name").asString()
                    val roleClaim = jwt.getClaim("role").asString()

                    val roleNameMatchResult = Regex("name=(\\w+)").find(roleClaim)
                    roleName = roleNameMatchResult?.groupValues?.get(1).toString()
                    hasToken = mutableStateOf(true)
                } catch (e: JWTDecodeException) {
                    println("Erro ao decodificar o token: ${e.message}")
                }
            } else {
                println("Ainda não há token")
            }
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

    fun onBackPressed( navController: NavController){
        navController.popBackStack()
    }


    fun logout(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPreferences =
                context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("jwtToken", "")

            if (!token.isNullOrBlank()) {
                try {
                    sharedPreferences.edit().remove("jwtToken").apply()
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)

                } catch (e: JWTDecodeException) {
                    println("Erro ao decodificar o token: ${e.message}")
                }
            } else {
                println("Ainda não há token")
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun deleteAccount(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("jwtToken", "").toString()
        val decodedJWT: DecodedJWT = JWT.decode(token)
        val userId = decodedJWT.subject
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val status = deleteRepository.deleteUser(userId)
                viewModelScope.launch(Dispatchers.Main) {
                    deleteStatus.value = status
                    Toast.makeText(
                        context,
                        status,
                        Toast.LENGTH_SHORT
                    ).show()
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                    if (status == "Conta deletada com sucesso") {
                        viewModelScope.launch(Dispatchers.IO) {
                            sharedPreferences.edit().remove("jwtToken").apply()
                            val intent = Intent(context, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun desactivateAccount(context: Context){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("jwtToken", "").toString()
        val decodedJWT: DecodedJWT = JWT.decode(token)
        val userId = decodedJWT.subject
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            val status = desactivateRepository.desactivateUser(userId)
                viewModelScope.launch(Dispatchers.Main) {
                    deleteStatus.value = status
                    Toast.makeText(
                        context,
                        status,
                        Toast.LENGTH_SHORT
                    ).show()
                    withContext(Dispatchers.Main) {
                        isLoading = false
                    }
                    if (status == "Conta desativada com sucesso") {
                        viewModelScope.launch(Dispatchers.IO) {
                            sharedPreferences.edit().remove("jwtToken").apply()
                            val intent = Intent(context, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }
                    }
            }
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

    fun getCityNameFromLocation(context: Context, location: Location): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                addresses[0]?.subAdminArea  ?: addresses[0]?.locality
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
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

}
