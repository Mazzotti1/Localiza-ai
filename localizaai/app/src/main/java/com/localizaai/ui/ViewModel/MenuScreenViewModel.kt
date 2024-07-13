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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.localizaai.Model.PlaceInfo
import com.localizaai.Model.PlaceRequest
import com.localizaai.Model.SpecificPlaceResponse
import com.localizaai.Model.TrafficResponse
import com.localizaai.Model.WeatherResponse
import com.localizaai.data.repository.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class MenuScreenViewModel(private val context: Context) : ViewModel() {

    val themeMode = mutableStateOf(true)
    var username by mutableStateOf("")
    var roleName by mutableStateOf("")

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val placesRepository = PlacesRepository(context)
    var placesResponse by mutableStateOf<List<PlaceRequest>?>(null)
    var specificPlaceResponse by mutableStateOf<SpecificPlaceResponse?>(null)
    var infosPlaceResponse by mutableStateOf<PlaceInfo?>(null)
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

    fun loadPlacesAround(context: Context, location: Location){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val lat = location.latitude
        val lon = location.longitude
        val radius = "350" // permitir deixar dinamico depois
        val sort = "POPULARITY"
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val result = placesRepository.fetchPlacesData(lat.toString(), lon.toString(), radius, sort)

                result.onSuccess { responseBody ->

                    val placeJson = responseBody.toString()
                    val gson = Gson()
                    val placeListType = object : TypeToken<List<PlaceRequest>>() {}.type
                    val placeResponse: List<PlaceRequest> = gson.fromJson(placeJson, placeListType)

                    placesResponse = placeResponse
                    preparePlacesData(context, placesResponse)
                    Log.d("PlacesApi", "Resultado da consulta dos locais é: $placesResponse")
                }.onFailure { exception ->
                    Log.d("PlacesApi", "Error: ${exception.message}")
                }
            }
        }catch(e: Throwable){
            Log.d("PlacesApi", "Error: ${e}")
        }

    }

    fun preparePlacesData(context : Context, response : List<PlaceRequest>?){
        if (!response.isNullOrEmpty()) {
            for (place in response){
                getSpecificPlaceData(place)
            }
        }
    }

    fun getSpecificPlaceData(place : PlaceRequest){

        try {
            viewModelScope.launch(Dispatchers.IO) {
                val result = placesRepository.fetchSpecificPlacesData(place.fsqId)

                result.onSuccess { responseBody ->

                    val specificPlaceJson = responseBody.toString()
                    val gson = Gson()
                    val parsedResponse = gson.fromJson(specificPlaceJson, SpecificPlaceResponse::class.java)
                    specificPlaceResponse = parsedResponse

                    Log.d("PlacesApi", "Resultado da consulta dos locais especificos: $specificPlaceResponse")
                }.onFailure { exception ->
                    Log.d("PlacesApi", "Error: ${exception.message}")
                }
            }
        }catch (e: Throwable){
            Log.d("PlacesApi", "Error: ${e}")
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

                    Log.d("PlacesApi", "Resultado da consulta das informações do lugar: $infosPlaceResponse")
                }.onFailure { exception ->
                    Log.d("PlacesApi", "Error: ${exception.message}")
                }
            }
        }catch(e: Throwable){
            Log.d("PlacesApi", "Error: ${e}")
        }
    }
}
