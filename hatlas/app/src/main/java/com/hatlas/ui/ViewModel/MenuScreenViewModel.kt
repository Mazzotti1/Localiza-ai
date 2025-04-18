package com.hatlas.ui.ViewModel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.common.util.concurrent.RateLimiter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.maps.android.heatmaps.WeightedLatLng
import com.hatlas.Model.Autocomplete
import com.hatlas.Model.EventsRequest
import com.hatlas.Model.FeatureCollection
import com.hatlas.Model.HistoryRequest
import com.hatlas.Model.HistoryResponse
import com.hatlas.Model.PlaceInfo
import com.hatlas.Model.PlaceRequest
import com.hatlas.Model.SpecificPlaceResponse
import com.hatlas.Model.SuggestionResponse
import com.hatlas.Model.Traffic
import com.hatlas.Model.TrafficResponse
import com.hatlas.Model.Weather
import com.hatlas.Model.WeatherResponse
import com.hatlas.R
import com.hatlas.data.repository.EventsRepository
import com.hatlas.data.repository.HistoryRespository
import com.hatlas.data.repository.MapboxRepository
import com.hatlas.data.repository.PlacesRepository
import com.hatlas.data.repository.TrafficRepository
import com.hatlas.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MenuScreenViewModel(private val context: Context) : ViewModel() {

    val themeMode = mutableStateOf(true)
    var userId by mutableStateOf("")
    var username by mutableStateOf("")
    var roleName by mutableStateOf("")

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val placesRepository = PlacesRepository(context)
    private val eventsRepository = EventsRepository(context)
    private val weatherRepository = WeatherRepository(context)
    private val trafficRepository = TrafficRepository(context)
    private val historyRepository = HistoryRespository(context)
    private val mapboxRepository = MapboxRepository(context)

    var placesResponse by mutableStateOf<List<PlaceRequest>?>(null)
    var specificPlaceResponse by mutableStateOf<SpecificPlaceResponse?>(null)
    var specificPlaceList = mutableStateListOf<SpecificPlaceResponse>()
    var infosPlaceResponse by mutableStateOf<PlaceInfo?>(null)
    var infosPlaceResponseForCalculate by mutableStateOf<PlaceInfo?>(null)
    var autocompletePlaces = mutableStateOf<Autocomplete?>(null)

    val placeSemaphore = Semaphore(7)
    val placeCache = mutableMapOf<String, SpecificPlaceResponse>()
    val rateLimiter = RateLimiter.create(7.0)

    private var shouldFreeCache : Boolean = false
    private var isFirstUpdate : Boolean = true
    var previousLat : Double = 0.0
    var previousLong : Double = 0.0
    var currentLat : Double = 0.0
    var currentLong : Double = 0.0

    var shouldStopUpdateUserLocation = mutableStateOf(false)
    private val _showHeatMap = MutableStateFlow(false)
    val showHeatMap: StateFlow<Boolean> = _showHeatMap
    private val _changeMapView = MutableStateFlow(false)
    val changeMapView: StateFlow<Boolean> = _changeMapView

    private val _changeHideView = MutableStateFlow(false)
    val changeHideView: StateFlow<Boolean> = _changeHideView

    val shouldMoveCamera = mutableStateOf(true)
    val shouldRegisterUpdate = mutableStateOf(true)
    var shouldMoveCameraToNewDestiny = mutableStateOf(false)
    var newLatLng = mutableStateOf<LatLng?>(null)
    var selectedPlace = mutableStateOf<String>("")

    var isDialogPlaceOpen = mutableStateOf(false)
    var showSearchListItens = mutableStateOf(false)

    private val _clickedLatLng = MutableLiveData<Pair<Double, Double>>()
    val clickedLatLng: LiveData<Pair<Double, Double>> = _clickedLatLng
    var selectedRadiusFilter = mutableStateOf<String>("Padrão")

    var language = mutableStateOf<String>("")
    var weatherResponse by mutableStateOf<WeatherResponse?>(null)
    var trafficResponse by mutableStateOf<TrafficResponse?>(null)
    var eventsResponse by mutableStateOf<List<EventsRequest>>(emptyList())
    var historyResponse by mutableStateOf<HistoryResponse?>(null)
    var mapboxAutocompleteResponse by mutableStateOf<SuggestionResponse?>(null)
    var mapboxSelectedDataResponse by mutableStateOf<FeatureCollection?>(null)

    private var locationCallback: LocationCallback? = null
    var isHoliday = mutableStateOf(false)
    var isLoadingPlaceInfo by mutableStateOf(false)
    var isLoadingSearch by mutableStateOf(false)

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

                    userId = jwt.getClaim("sub").asString()

                } catch (e: JWTDecodeException) {
                    println("Erro ao decodificar o token: ${e.message}")
                }
            } else {
                println("Ainda não há token")
            }
        }
    }

    fun onBackPressed( navController: NavController){
        shouldMoveCamera.value = true
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

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    onLocationUpdate(location)

                    if (isFirstUpdate) {
                        previousLat = location.latitude
                        previousLong = location.longitude
                        isFirstUpdate = false
                    } else {
                        currentLat = location.latitude
                        currentLong = location.longitude
                    }

                    if (currentLong != 0.0 && currentLat != 0.0) {
                        compareTravelledDistance()
                    }

                    if (shouldFreeCache) {
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
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback as LocationCallback, Looper.getMainLooper())
        }
    }

    fun stopLocationUpdates(fusedLocationProviderClient: FusedLocationProviderClient) {
        locationCallback?.let {
            fusedLocationProviderClient.removeLocationUpdates(it)
        }
        locationCallback = null
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPlacesAround(context: Context, location: Location, logType : String = "Places Api"){
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

        val lat = location.latitude
        val lon = location.longitude
        var radiusList = listOf("")

        if(selectedRadiusFilter.value ==  "Padrão"){
            radiusList = listOf("300", "600", "800")
        } else {
            val radiusValue = selectedRadiusFilter.value.replace("m", "")
            radiusList = listOf(radiusValue)
        }

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
                                logType,
                                "Resultado da consulta dos locais é: $placesResponse"
                            )
                        }.onFailure { exception ->
                            Log.d(logType, "Error 2: ${exception.message}")
                        }
                    } catch (e: Throwable) {
                        Log.d(logType, "Error 2: ${e}")
                    }
                }
                delay(1000L)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun preparePlacesData(response: List<PlaceRequest>?) {
        if (!response.isNullOrEmpty()) {
            viewModelScope.launch {
                response.forEach { place ->
                    placeSemaphore.withPermit {
                        launch(Dispatchers.IO) {
                            getSpecificPlaceData(place)
                        }
                    }
                    delay(1500L)
                }
            }
        }
    }

    suspend fun fetchSpecificPlaceDataWithRetry(fsqId: String, retries: Int = 1): Result<String> {
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("language", "en") ?: "en"
        language.value = savedLanguage
        repeat(retries) {
            if (rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
                val result = placesRepository.fetchSpecificPlacesData(fsqId, savedLanguage)
                if (result.isSuccess) {
                    return result
                }
            }
        }
        return Result.failure(Exception("Failed after $retries retries"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

                    saveHistoryPlace(parsedResponse)
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

    fun getAllPlaceInfo(name: String, lat: String, long: String) {
        viewModelScope.launch {
            isLoadingPlaceInfo = true

            try {
                withContext(Dispatchers.IO) {
                    val result = placesRepository.fetchPlaceByNameData(lat, long, name)

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
            } catch (e: Throwable) {
                Log.d("PlacesApi", "Error: ${e.message}")
            } finally {
                isLoadingPlaceInfo = false
            }
        }
    }


    fun freeCacheData(){
        if(specificPlaceList.isNotEmpty()){
            specificPlaceList.clear()
            placeCache.clear()
            shouldFreeCache = false
        }
    }

    @SuppressLint("SuspiciousIndentation")
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

    fun onSearch(search : String){
        val lat = currentLat.toString()
        val long = currentLong.toString()
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val result = placesRepository.fetchPlaceAutocomplete(search,lat,long)

                result.onSuccess { responseBody ->

                    val autocompleteJson = responseBody.toString()
                    val gson = Gson()
                    val parsedResponse = gson.fromJson(autocompleteJson, Autocomplete::class.java)

                    autocompletePlaces.value = parsedResponse

                    Log.d("PlacesApiAutocomplete", "Search value : $search, lat: $lat, long: $long")
                    Log.d("PlacesApiAutocomplete", "Resultado da consulta dos locais de pesquisa: ${autocompletePlaces.value}")
                }.onFailure { exception ->
                    Log.d("PlacesApi", "Error: ${exception.message}")
                }
                if (autocompletePlaces.value?.results?.isEmpty() == true) {
                    showSearchListItens.value = false
                }
            }
        }catch(e: Throwable){
            Log.d("PlacesApi", "Error: ${e}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSelectSearchListItem(context: Context, lat: String, long: String, itemName : String){
        val latDouble = lat.toDoubleOrNull() ?: 0.0
        val longDouble = long.toDoubleOrNull() ?: 0.0
        selectedPlace.value = itemName;
        val shouldCamFreeCache = compareCamItemdistance(latDouble,longDouble)

        if(shouldCamFreeCache){
            freeCacheData()
            shouldStopUpdateUserLocation.value = true //verificar quando volta ao estado normal de false provavelmente quando eu clicar no botão de voltar
        }

        val location = createLocation(latDouble,longDouble)
        loadPlacesAround(context, location)

        newLatLng.value = LatLng(latDouble, longDouble)
        showSearchListItens.value = false
        shouldMoveCameraToNewDestiny.value = true

        isLoadingSearch = true
    }

    private fun compareCamItemdistance(nextLat : Double, nextLong : Double) : Boolean{
        val earthRadius = 6371e3
        if(currentLong != 0.0  && currentLat != 0.0  && nextLat != 0.0  && nextLong != 0.0 ){
            val dLat = Math.toRadians(currentLat - nextLat)
            val dLon = Math.toRadians(currentLong - nextLong)

            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(nextLat)) * cos(Math.toRadians(currentLat)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            val distance =  earthRadius * c
            if(distance > 1200){
                return true
            } else{
                return false
            }
        }
        return false
    }

    private fun createLocation(lat : Double, long: Double): Location {
        val location = Location("CustomProvider")
        location.latitude = lat
        location.longitude = long
        return location
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onResetButtonClick(): LatLng {

        val currentLatLng: LatLng = if (currentLat != 0.0 && currentLong != 0.0) {
            freeCacheData()
            shouldStopUpdateUserLocation.value = false

            val location = createLocation(currentLat,currentLong)
            loadPlacesAround(context, location)

            LatLng(currentLat, currentLong)

        } else {
            freeCacheData()
            shouldStopUpdateUserLocation.value = false

            val location = createLocation(previousLat,previousLong)
            loadPlacesAround(context, location)

            LatLng(previousLat, previousLong)
        }

        return currentLatLng
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun updateClickedLatLng(latitude: Double, longitude: Double) {
        _clickedLatLng.value = Pair(latitude, longitude)

        val shouldClickFreeCache = compareCamItemdistance(latitude,longitude)

        if(shouldClickFreeCache){
            freeCacheData()
            shouldStopUpdateUserLocation.value = true
        }

        val location = createLocation(latitude,longitude)
        loadPlacesAround(context, location)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onHeatMapChange(){
        val heatMap = "HeatMap"
        _showHeatMap.value = !_showHeatMap.value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onMapViewChange(){
        _changeMapView.value = !_changeMapView.value
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onMapHideChange(){
        _changeHideView.value = !_changeHideView.value
    }

    fun getEventsData(){
        val localRequest : String
        when (language.value) {
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
                val eventsJson = responseBody.toString()
                val gson = Gson()
                val type = object : TypeToken<List<EventsRequest>>() {}.type
                eventsResponse = gson.fromJson(eventsJson, type)

                Log.d("EventsApi", "Resultado da consulta de eventos é: ${result.toString()}")
            }.onFailure { exception ->
                Log.d("EventsApi", "Error: ${exception.message}")
            }
        }
    }

    fun getWeatherData (location : LatLng){
        val cityName = getCityNameFromLocation(context, location ).toString()
        val days = 1
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val lang = language

        viewModelScope.launch(Dispatchers.IO) {
            val result = weatherRepository.fetchWeatherData(cityName, hour, days, lang.toString())

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

    fun getCityNameFromLocation(context: Context, location: LatLng): String? {
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


    fun getTrafficData(location: LatLng){
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getHeatmapData(): List<WeightedLatLng> {

        val fixedPoints = listOf(
            WeightedLatLng(LatLng(previousLat, previousLong), 0.0)
        )

        val dynamicPoints = specificPlaceList.map { place ->
            val currentHour = LocalTime.now().hour
            val threshold = if (isPeakTime(place)) 0.7 else 0.5
            val latitude = place.geocodes?.main?.latitude ?: 0.0
            val longitude = place.geocodes?.main?.longitude ?: 0.0
            val score = place.score ?: 0.1
            Log.d("HEATMAP", "${score}")
            val adjustedScore = if (currentHour >= 23 || currentHour < 6) {
                score * 0.5
            } else if (score > threshold) {
                score * 2
            } else {
                score * 0.5
            }
            WeightedLatLng(LatLng(latitude, longitude), adjustedScore)
        }


        return fixedPoints + dynamicPoints
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveHistoryPlace(parsedResponse: SpecificPlaceResponse){


        val categories = parsedResponse.categories ?: emptyList()
        var cat = ""

        if (categories.isNotEmpty()) {
            for (i in categories) {
                cat = i.name
                break
            }
        }

        val category: String = cat.ifEmpty {
            "noCategory"
        }


        val currentTimestamp = OffsetDateTime.now().toString()

        val trafficData = if (trafficResponse != null) {
            Traffic(
                currentSpeed = trafficResponse!!.currentSpeed ?: 0,
                freeFlowSpeed = trafficResponse!!.freeFlowSpeed ?: 0,
                currentTravelTime = trafficResponse!!.currentTravelTime ?: 0,
                freeFlowTravelTime = trafficResponse!!.freeFlowTravelTime ?: 0,
                confidence = trafficResponse!!.confidence ?: 0.0,
                roadClosure = trafficResponse!!.roadClosure ?: false
            )
        } else {
            Traffic(
                currentSpeed = 0,
                freeFlowSpeed = 0,
                currentTravelTime = 0,
                freeFlowTravelTime = 0,
                confidence = 0.0,
                roadClosure = false
            )
        }

        val weatherData = if (weatherResponse != null) {
            Weather(
                condition = weatherResponse!!.current.condition.text ?: "Unknown",
                temperature = weatherResponse!!.current.temp_c ?: 0.0,
                humidity = weatherResponse!!.current.humidity ?: 0,
                rainChance = weatherResponse!!.forecast.forecastday.getOrNull(0)?.hour?.getOrNull(0)?.chance_of_rain ?: 0
            )
        } else {
            Weather(
                condition = "Unknown",
                temperature = 0.0,
                humidity = 0,
                rainChance = 0
            )
        }

        val historyRequest = HistoryRequest(
            historyTimestamp = currentTimestamp,
            name = parsedResponse.name ?: "Unknown",
            fsqId = parsedResponse.fsq_id,
            description = parsedResponse.closed_bucket ?: "No description",
            entityType = "place",
            latitude = parsedResponse.geocodes?.main?.latitude ?: 0.0,
            longitude = parsedResponse.geocodes?.main?.longitude ?: 0.0,
            category = category  ?: "Unknown",
            updatedBy = userId.toLong()  ?: 3,
            weather = weatherData,
            traffic = trafficData
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = historyRepository.setHistoryData(historyRequest)

            result.onSuccess { responseBody ->
                val historyJson = responseBody.toString()
                val gson = Gson()
                historyResponse = gson.fromJson(historyJson, HistoryResponse::class.java)

                Log.d("HistoryApi", "Resultado da inserção de lugar: ${result.toString()}")
            }.onFailure { exception ->
                Log.d("HistoryApi", "Error: ${exception.message}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun isPeakTime(place: SpecificPlaceResponse): Boolean {
        val currentHour = LocalTime.now().hour
        val categoryType = place.categoryType

        return when (categoryType) {
            "Dining and Drinking" -> currentHour in 18..22
            "Retail" -> currentHour in 10..21
            "Business and Professional Services" -> currentHour in 9..18
            "Sports and Recreation" -> currentHour in 16..20
            "Health and Medicine" -> currentHour in 8..17
            "Arts and Entertainment" -> currentHour in 18..23
            "Travel and Transportation" -> currentHour in 6..9 || currentHour in 17..20
            "Community and Government" -> currentHour in 8..17
            "Event" -> currentHour in 18..23  // eventos à noite
            else -> currentHour in 11..13 || currentHour in 18..20
        }
    }

    fun customIconByCategory(place: SpecificPlaceResponse) : BitmapDescriptor{
        val categoryType = place.categoryType
        var icon : Int = R.drawable.pin_marker_sports

        when(categoryType){
            "Landmarks and Outdoors" -> icon = R.drawable.pin_marker_sports
            "Retail" -> icon = R.drawable.pin_marker_market
            "Business and Professional Services" -> icon = R.drawable.pin_marker_work
            "Sports and Recreation" -> icon = R.drawable.pin_marker_sports
            "Community and Government" -> icon = R.drawable.pin_marker_work
            "Dining and Drinking" -> icon = R.drawable.pin_marker_food
            "Event" -> icon = R.drawable.pin_marker_events
            "Health and Medicine" -> icon = R.drawable.pin_marker_health
            "Arts and Entertainment" -> icon = R.drawable.pin_marker_art
            "Travel and Transportation" -> icon = R.drawable.pin_marker_travel
            else -> icon = R.drawable.pin_marker_medium
        }

            val customIconBitmap: Bitmap = BitmapFactory.decodeResource(context.resources, icon)
            val customMatchedIconPlace = BitmapDescriptorFactory.fromBitmap(customIconBitmap)

        return  customMatchedIconPlace
    }

    fun getMapBoxAutocompletes(query:String){
        viewModelScope.launch(Dispatchers.IO) {
            val result = mapboxRepository.fetchMapBoxAutocompletes(query)

            result.onSuccess { responseBody ->

                val mapBoxAutocomplete = responseBody.toString()
                val gson = Gson()
                mapboxAutocompleteResponse = gson.fromJson(mapBoxAutocomplete, SuggestionResponse::class.java)

                Log.d("MapBoxApi", "Resultado do autocomplete é: ${result.toString()}")
            }.onFailure { exception ->
                Log.d("MapBoxApi", "Error: ${exception.message}")
            }
            if(mapboxAutocompleteResponse?.suggestions!!.isEmpty()){
                showSearchListItens.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMapBoxSelectedData(context: Context, query:String){
        viewModelScope.launch(Dispatchers.IO) {
            val result = mapboxRepository.fetchMapBoxPlaceData(query)

            result.onSuccess { responseBody ->

                val mapBoxSelectedData = responseBody.toString()
                val gson = Gson()
                mapboxSelectedDataResponse = gson.fromJson(mapBoxSelectedData, FeatureCollection::class.java)

                onSelectGeneralSearchListItem(context)

                Log.d("MapBoxApi", "Resultado do local selecionado é: ${result.toString()}")
            }.onFailure { exception ->
                Log.d("MapBoxApi", "Error: ${exception.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onSelectGeneralSearchListItem(context: Context){
        val data = mapboxSelectedDataResponse
        val cordinates = data!!.features[0].properties.coordinates
        val latDouble = cordinates.latitude
        val longDouble = cordinates.longitude

        val shouldCamFreeCache = compareCamItemdistance(latDouble,longDouble)

        if(shouldCamFreeCache){
            freeCacheData()
            shouldStopUpdateUserLocation.value = true
        }

        val location = createLocation(latDouble,longDouble)
        loadPlacesAround(context, location)

        newLatLng.value = LatLng(latDouble, longDouble)
        showSearchListItens.value = false
        shouldMoveCameraToNewDestiny.value = true

        isLoadingSearch = true
    }
}



