package com.localizaai.ui.ViewModel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.common.util.concurrent.RateLimiter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.localizaai.Model.PlaceInfo
import com.localizaai.Model.PlaceRequest
import com.localizaai.Model.SpecificPlaceResponse
import com.localizaai.Model.TrafficResponse
import com.localizaai.Model.WeatherResponse
import com.localizaai.data.repository.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.ResponseBody
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MenuScreenViewModel(private val context: Context) : ViewModel() {

    val themeMode = mutableStateOf(true)
    var username by mutableStateOf("")
    var roleName by mutableStateOf("")

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val placesRepository = PlacesRepository(context)
    var placesResponse by mutableStateOf<List<PlaceRequest>?>(null)
    var specificPlaceResponse by mutableStateOf<SpecificPlaceResponse?>(null)
    var specificPlaceList = mutableStateListOf<SpecificPlaceResponse>()
    var infosPlaceResponse by mutableStateOf<PlaceInfo?>(null)
    val placeSemaphore = Semaphore(7)
    val placeCache = mutableMapOf<String, SpecificPlaceResponse>()
    val rateLimiter = RateLimiter.create(7.0)
    private var shouldFreeCache : Boolean = false
    private var isFirstUpdate : Boolean = true
    private var previousLat : Double = 0.0
    private var previousLong : Double = 0.0
    private var currentLat : Double = 0.0
    private var currentLong : Double = 0.0
    var isDialogPlaceOpen = mutableStateOf(false)
    fun loadThemeState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPreferences =
                context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
            themeMode.value = sharedPreferences.getBoolean("mode", true)
        }
    }
    fun getTokenProps(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
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

                } catch (e: JWTDecodeException) {
                    println("Erro ao decodificar o token: ${e.message}")
                }
            } else {
                println("Ainda não há token")
            }
        }
    }

    fun onBackPressed( navController: NavController){
            navController.popBackStack()
    }

    fun startLocationUpdates(
        fusedLocationProviderClient: FusedLocationProviderClient,
        context: Context,
        onLocationUpdate: (Location) -> Unit
    ) {
        val locationRequest = LocationRequest.create().apply {
            interval = 6000 // 10 segundos
            fastestInterval = 3000 // 5 segundos
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    onLocationUpdate(location)

                    if(isFirstUpdate){
                        previousLat = location.latitude
                        previousLong = location.longitude
                        isFirstUpdate = false
                    }else{
                        currentLat = location.latitude
                        currentLong = location.longitude
                    }

                    if(currentLong != 0.0 && currentLat != 0.0 ){
                        compareTravelledDistance()
                    }

                    if(shouldFreeCache){
                        freeCacheData()
                    }
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

    fun startPlacesLocationUpdates(
        fusedLocationProviderClient: FusedLocationProviderClient,
        context: Context,
        onLocationUpdate: (Location) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val locationRequest = LocationRequest.create().apply {
                interval = 60 * 1000 * 3// 3min
                fastestInterval = 60 * 1000 * 3 // 3min
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
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }
    }

    fun loadPlacesAround(context: Context, location: Location){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val lat = location.latitude
        val lon = location.longitude
        val radiusList = listOf("300", "600", "800")// permitir deixar dinamico depois
        val sort = "POPULARITY"


        viewModelScope.launch(Dispatchers.IO) {
            radiusList.forEach { radius ->
                placeSemaphore.withPermit {

                    try {
                        val result = placesRepository.fetchPlacesData(
                            lat.toString(),
                            lon.toString(),
                            radius,
                            sort
                        )
                        result.onSuccess { responseBody ->
                            val placeJson = responseBody.toString()
                            val gson = Gson()
                            val placeListType = object : TypeToken<List<PlaceRequest>>() {}.type
                            val placeResponse: List<PlaceRequest> =
                                gson.fromJson(placeJson, placeListType)

                            placesResponse = placeResponse
                            preparePlacesData(placesResponse)
                            Log.d(
                                "PlacesApi",
                                "Resultado da consulta dos locais é: $placesResponse"
                            )
                        }.onFailure { exception ->
                            Log.d("PlacesApi", "Error 2: ${exception.message}")
                        }
                    } catch (e: Throwable) {
                        Log.d("PlacesApi", "Error 2: ${e}")
                    }
                }
                delay(1000L)
            }
        }
    }

    fun preparePlacesData(response: List<PlaceRequest>?) {
        if (!response.isNullOrEmpty()) {
            viewModelScope.launch {
                response.forEach { place ->
                    placeSemaphore.withPermit {
                        launch(Dispatchers.IO) {
                            getSpecificPlaceData(place)
                        }
                    }
                    delay(1000L)
                }
            }
        }
    }

    suspend fun fetchSpecificPlaceDataWithRetry(fsqId: String, retries: Int = 1): Result<String> {
        repeat(retries) {
            if (rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
                val result = placesRepository.fetchSpecificPlacesData(fsqId)
                if (result.isSuccess) {
                    return result
                }
            }
        }
        return Result.failure(Exception("Failed after $retries retries"))
    }

    fun getSpecificPlaceData(place : PlaceRequest){
        val cachedPlace = placeCache[place.fsqId]
        if (cachedPlace != null) {
            specificPlaceResponse = cachedPlace
            specificPlaceList.add(specificPlaceResponse!!)

            Log.d("PlacesApi", "Usando o valor em cache para: ${place.fsqId}")
            return
        }
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val result = fetchSpecificPlaceDataWithRetry(place.fsqId)

                result.onSuccess { responseBody ->

                    val specificPlaceJson = responseBody.toString()
                    val gson = Gson()
                    val parsedResponse = gson.fromJson(specificPlaceJson, SpecificPlaceResponse::class.java)
                    specificPlaceList.add(parsedResponse)
                    placeCache[place.fsqId] = parsedResponse
                    Log.d("PlacesApi", "Resultado da consulta dos locais especificos: $specificPlaceList")
                }.onFailure { exception ->
                    Log.d("PlacesApi", "Error 1: ${exception.message}")
                }
            }
        }catch (e: Throwable){
            Log.d("PlacesApi", "Error 1: ${e}")
        }
    }

    // chamar ao clicar e escolher um lugar especifico
    fun getAllPlaceInfo(name : String, lat: String, long : String){
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val result = placesRepository.fetchPlaceByNameData(lat,long,name)

                result.onSuccess { responseBody ->

                    val infosPlaceJson = responseBody.toString()
                    val gson = Gson()
                    val parsedResponse = gson.fromJson(infosPlaceJson, PlaceInfo::class.java)

                    infosPlaceResponse = parsedResponse
                    isDialogPlaceOpen.value = true

                    Log.d("PlacesApi", "Resultado da consulta das informações do lugar: $infosPlaceResponse")
                }.onFailure { exception ->
                    Log.d("PlacesApi", "Error: ${exception.message}")
                }
            }
        }catch(e: Throwable){
            Log.d("PlacesApi", "Error: ${e}")
        }
    }

    fun freeCacheData(){
        if(specificPlaceList.isNotEmpty()){
            specificPlaceList.clear()
            placeCache.clear()
            shouldFreeCache = false
        }
    }

    fun compareTravelledDistance(){
        val earthRadius = 6371e3
        if(currentLong != 0.0  && currentLat != 0.0  && previousLat != 0.0  && previousLong != 0.0 ){
            val dLat = Math.toRadians(currentLat - previousLat)
            val dLon = Math.toRadians(currentLong - previousLong)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(previousLat)) * cos(Math.toRadians(currentLat)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val distance =  earthRadius * c
            if(distance > 1500){
                shouldFreeCache = true
                isFirstUpdate = true
            }
            Log.d("PlacesApi", "Distancia percorrida ate agorar: ${distance}")
        }
    }
}
