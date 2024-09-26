package com.localizaai.ui.ViewModel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.google.android.gms.maps.model.LatLng
import com.google.common.util.concurrent.RateLimiter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.localizaai.Model.Autocomplete
import com.localizaai.Model.CategoryData
import com.localizaai.Model.EventsRequest
import com.localizaai.Model.HistoryRequest
import com.localizaai.Model.HistoryResponse
import com.localizaai.Model.PlaceInfo
import com.localizaai.Model.PlaceRequest
import com.localizaai.Model.ScoreCategoryResponse
import com.localizaai.Model.SpecificPlaceResponse
import com.localizaai.Model.Traffic
import com.localizaai.Model.TrafficResponse
import com.localizaai.Model.Weather
import com.localizaai.Model.WeatherResponse
import com.localizaai.data.repository.EventsRepository
import com.localizaai.data.repository.HistoryRespository
import com.localizaai.data.repository.PlacesRepository
import com.localizaai.data.repository.TrafficRepository
import com.localizaai.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
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
    var scoreCategoryResponse by mutableStateOf<ScoreCategoryResponse?>(null)

    private var locationCallback: LocationCallback? = null
    var isHoliday = mutableStateOf(false)

    var placeType = ""
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
                if(autocompletePlaces.value?.results!!.isEmpty()){
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

        val location = LatLng(currentLat, currentLong)
        getEventsData()
        getWeatherData(location)
        getTrafficData(location)
        getBaseData()
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
                addresses[0]?.locality
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
    fun getActualTimestamp(): Pair<String, String> {
        val now = LocalDateTime.now()

        val dateFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val formattedDate = now.format(dateFormatter)
        val formattedTime = now.format(timeFormatter)

        return Pair(formattedDate, formattedTime)
    }

    fun getBaseData(){
        viewModelScope.launch(Dispatchers.IO) {

            val radius = when (selectedRadiusFilter.toString()) {
                "Padrão" -> 800
                else -> selectedRadiusFilter.toString().toIntOrNull() ?: 800
            }

            val result = historyRepository.fetchHistoryDataByLocation(currentLat,currentLong, radius.toString())

            result.onSuccess { responseBody ->
                val historyJson = responseBody.toString()
                val gson = Gson()
                historyResponse = gson.fromJson(historyJson, HistoryResponse::class.java)

                Log.d("HistoryApi", "Resultado da consulta do histórico por localização é: ${result.toString()}")
            }.onFailure { exception ->
                Log.d("HistoryApi", "Error: ${exception.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getHeatmapData(): List<LatLng?> {

        val fixedPoints = listOf(
            LatLng(previousLat, previousLong)
        )

        val threshold = if (isPeakTime()) 0.7 else 0.5

        //colocar a formação dos points dentro do forEach
        specificPlaceList.forEach{place ->
            val score = calculateLocalMovementScore(place)
        }


        val dynamicPoints = specificPlaceList.map { place ->
            val latitude = place.geocodes?.main?.latitude ?: 0.0
            val longitude = place.geocodes?.main?.longitude ?: 0.0
            LatLng(latitude, longitude)
        }

        return fixedPoints + dynamicPoints
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveHistoryPlace(parsedResponse: SpecificPlaceResponse){

        val categories: List<String> = parsedResponse.categories.map { it.name }
        val category: String = categories.joinToString("/ ")
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
            name = parsedResponse.name,
            description = parsedResponse.closed_bucket,
            entityType = "place",
            latitude = parsedResponse.geocodes?.main?.latitude ?: 0.0,
            longitude = parsedResponse.geocodes?.main?.longitude ?: 0.0,
            category = category,
            updatedBy = userId.toLong(),
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
    private fun calculateLocalMovementScore(place: SpecificPlaceResponse): Double {
        val holidayData = eventsResponse
        val weatherData = weatherResponse
        val trafficData = trafficResponse
        val historyData = historyResponse

        val rawHolidayScore = getHolidayScore(holidayData)
        val rawWeatherScore = getWeatherScore(weatherData)
        val rawTrafficScore = getTrafficScore(trafficData)
        val rawHistoryScore = getHistoryScore(historyData)

        val rawScores = listOf(rawHolidayScore, rawWeatherScore, rawTrafficScore, rawHistoryScore)
        val normalizedScores = normalizeScores(rawScores)

        val placeCategoriesResult = getWeightsForPlaceType(place)
        val placeWeight = placeCategoriesResult.score
        checkIsHoliday(eventsResponse)

        var placeImpact = getPlaceScore(place)
        var weatherImpact = normalizedScores[1]
        var holidayImpact = normalizedScores[0]
        var trafficImpact = normalizedScores[2]
        val historyImpact = normalizedScores[3]

        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "")
        var conditionText = when (language){
            "en" -> "Clear"
            "pt" -> "Céu limpo"
            else -> ""
        }

        if(placeCategoriesResult.type == "OUTDOOR"){
            if (normalizedScores[1] < 0.5) {
                weatherImpact *= 0.5
            }

            weatherData!!.forecast.forecastday.forEach { weather ->
                weather.hour.forEach { hour ->
                    if (hour.is_day == 1) {
                        if (hour.condition.text.contains(conditionText, ignoreCase = true)) {
                            weatherImpact *= 1.2
                        } else {
                            weatherImpact *= 0.6
                        }
                    } else {
                        if (hour.condition.text.contains(conditionText, ignoreCase = true)) {
                            weatherImpact *= 1.1
                        } else {
                            weatherImpact *= 0.5
                        }
                    }
                }
            }

        } else if (placeCategoriesResult.type == "INDOOR"){
            if (normalizedScores[1] < 0.5) {
                placeImpact *= 1.2
            }

            weatherData!!.forecast.forecastday.forEach { weather ->
                weather.hour.forEach { hour ->
                    if (hour.is_day == 1) {
                        if (hour.condition.text.contains(conditionText, ignoreCase = true)) {
                            weatherImpact *= 1.2
                        } else {
                            weatherImpact *= 0.8
                        }
                    } else {
                        if (hour.condition.text.contains(conditionText, ignoreCase = true)) {
                            weatherImpact *= 1.1
                        } else {
                            weatherImpact *= 0.9
                        }
                    }
                }
            }
        }

        if (normalizedScores[1] < 0.5) {
            holidayImpact *= 0.7
        }

        if(isHoliday.value){
            if (normalizedScores[2] > 0.5) {
                trafficImpact *= 1.1
            }
        }

        //Usar o history impact pra tentar criar um modelo preditivo de machine learning la no backend

        val weightedScore = (
                (placeWeight * placeImpact) *
                        (placeWeight * holidayImpact) *
                        (placeWeight * weatherImpact) *
                        (placeWeight * trafficImpact) *
                        (placeWeight * historyImpact)
                )

        return weightedScore
    }

    private fun getPlaceScore(place: SpecificPlaceResponse): Double {
        var placeScore: Double = 0.0

        try {
            getAllPlaceInfoForCalculate(place.name, place.geocodes!!.main!!.latitude.toString(), place.geocodes!!.main!!.longitude.toString())

            val popularity = infosPlaceResponseForCalculate!!.place!!.popularity
            when {
                popularity < 0.3 -> placeScore -= 0.4  // Baixíssima popularidade
                popularity < 0.5 -> placeScore -= 0.2  // Popularidade moderada
                popularity < 0.7 -> placeScore += 0.1  // Popularidade razoável
                else -> placeScore += 0.4
            }

            val rating = infosPlaceResponseForCalculate!!.place!!.rating
            when {
                rating < 5.0 -> placeScore -= 0.5  // Rating muito baixo
                rating < 6.5 -> placeScore -= 0.2  // Rating baixo
                rating < 8.0 -> placeScore += 0.3  // Rating médio-alto
                else -> placeScore += 0.5
            }

            val verified = infosPlaceResponseForCalculate!!.place!!.verified
            placeScore += if (verified == true) 0.2 else -0.1

            val matchScore = infosPlaceResponseForCalculate!!.match_score
            when {
                matchScore < 0.4 -> placeScore -= 0.4  // Baixa correspondência
                matchScore < 0.7 -> placeScore -= 0.1  // Moderada
                matchScore < 0.9 -> placeScore += 0.2  // Boa
                else -> placeScore += 0.5
            }

            when (place.closed_bucket) {
                "VeryLikelyOpen" -> {
                    placeScore += 0.5
                }
                "VeryLikelyClosed" -> {
                    placeScore -= 0.5
                }
                "LikelyOpen" -> {
                    placeScore += 0.3
                }
                "LikelyClosed" -> {
                    placeScore -= 0.3
                }
            }

        } catch (e: Throwable) {
        }

        return placeScore
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getHolidayScore(eventData: List<EventsRequest>): Double {
        var score: Double = 0.0
        try {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            eventData.forEach { data ->
                val eventDate = LocalDate.parse(data.second, formatter)
                val daysDifference = ChronoUnit.DAYS.between(currentDate, eventDate).toInt()

                when {
                    daysDifference == 0 -> {
                        score = 1.0
                    }
                    daysDifference in 1..7 -> {
                        score = 0.5
                    }
                    daysDifference > 7 -> {
                        score = 0.2
                    }
                }
            }
        } catch (e: Throwable) {
        }
        return score
    }


    private fun getWeatherScore(weatherData: WeatherResponse?): Double {
        var score: Double = 0.0
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "")
        val conditionsPt = listOf("Céu limpo", "parcialmente nublado", "possibilidade de chuva", "chuva")
        val conditionsEn = listOf("Clear", "Partly cloudy", "Chance of rain", "Rain")

        var condition = when (language){
            "en" -> conditionsEn
            "pt" -> conditionsPt
            else -> listOf("")
        }

        try {
            // Temperatura
            val currentTemp = weatherData?.current?.temp_c ?: 0.0
            when {
                currentTemp < 15 -> score -= 0.5 // Muito frio
                currentTemp in 15.0..19.9 -> score += 0.2 // Frio
                currentTemp in 20.0..25.0 -> score += 1.0 // Confortável
                currentTemp in 25.1..30.0 -> score += 0.5 // Quente
                currentTemp > 30 -> score -= 0.5 // Muito quente
            }

            // Umidade
            val humidity = weatherData?.current?.humidity ?: 0
            if (humidity > 80) {
                score -= 0.3 // Alta umidade
            }

            // Condições climáticas
            val conditionText = weatherData?.current?.condition?.text ?: ""
            when {
                conditionText.contains(condition[0], ignoreCase = true) -> score += 1.0
                conditionText.contains(condition[1], ignoreCase = true) -> score += 0.5
                conditionText.contains(condition[2], ignoreCase = true) -> score -= 0.5
                conditionText.contains(condition[3], ignoreCase = true) -> score -= 1.0
            }

            // Previsão de chuva
            val dailyChanceOfRain = weatherData?.forecast?.forecastday?.firstOrNull()?.day?.daily_chance_of_rain ?: 0
            if (dailyChanceOfRain > 50) {
                score -= 0.5
            }

        } catch (e: Throwable) {
        }
        return score
    }


    private fun getTrafficScore(trafficData: TrafficResponse?): Double {
        var score: Double = 0.0
        try {
            // Velocidade Atual vs. Velocidade de Fluxo Livre
            val currentSpeed = trafficData?.currentSpeed ?: 0
            val freeFlowSpeed = trafficData?.freeFlowSpeed ?: 0
            if (currentSpeed > 0 && freeFlowSpeed > 0) {
                val speedRatio = currentSpeed.toDouble() / freeFlowSpeed.toDouble()
                when {
                    speedRatio >= 0.9 -> score += 1.0 // Tráfego fluindo bem (velocidade próxima à ideal)
                    speedRatio in 0.7..0.89 -> score += 0.5 // Tráfego moderado
                    speedRatio in 0.5..0.69 -> score -= 0.5 // Tráfego pesado
                    speedRatio < 0.5 -> score -= 1.0 // Tráfego muito intenso
                }
            }

            // Tempo de Viagem Atual vs. Tempo de Fluxo Livre
            val currentTravelTime = trafficData?.currentTravelTime ?: 0
            val freeFlowTravelTime = trafficData?.freeFlowTravelTime ?: 0
            if (currentTravelTime > 0 && freeFlowTravelTime > 0) {
                val travelTimeRatio = currentTravelTime.toDouble() / freeFlowTravelTime.toDouble()
                when {
                    travelTimeRatio <= 1.1 -> score += 1.0 // Tempo de viagem normal
                    travelTimeRatio in 1.1..1.3 -> score += 0.5 // Pequeno atraso
                    travelTimeRatio in 1.3..1.5 -> score -= 0.5 // Atraso moderado
                    travelTimeRatio > 1.5 -> score -= 1.0 // Atraso significativo
                }
            }

            // Nível de Confiança
            val confidence: Double = trafficData?.confidence ?: 1.0
            when {
                confidence >= 0.9 -> score += 0.5
                confidence >= 0.7 && confidence < 0.9 -> score += 0.2
                confidence < 0.7 -> score -= 0.5
            }

            // Fechamento de Estrada
            val roadClosure = trafficData?.roadClosure ?: false
            if (roadClosure) {
                score -= 2.0
            }

        } catch (e: Throwable) {
        }

        return score
    }


    private fun getHistoryScore(historyData:HistoryResponse?):Double{
        return 0.0
    }

    private fun getWeightsForPlaceType(place: SpecificPlaceResponse): CategoryData {

        place.categories.forEach { category ->
            placeType = category.name
        }

        getScoreByCategory()

        if(scoreCategoryResponse!!.status){
            return scoreCategoryResponse!!.data
        }else {
            return CategoryData(0.0, "default")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isPeakTime(): Boolean {
        val currentHour = LocalTime.now().hour
        return currentHour in 11..13 || currentHour in 18..20
    }

    // media dos valores
    fun calculateMean(values: List<Double>): Double {
        return values.sum() / values.size
    }

    //desvio padrao
    fun calculateStandardDeviation(values: List<Double>, mean: Double): Double {
        val variance = values.map { (it - mean).pow(2) }.sum() / values.size
        return sqrt(variance)
    }

    // normalização Z
    fun normalizeZScore(value: Double, mean: Double, standardDeviation: Double): Double {
        return (value - mean) / standardDeviation
    }

    //normalizar um conjunto de dados
    fun normalizeScores(scores: List<Double>): List<Double> {
        val mean = calculateMean(scores)
        val stdDev = calculateStandardDeviation(scores, mean)

        return scores.map { normalizeZScore(it, mean, stdDev) }
    }

    private fun getScoreByCategory() {
        viewModelScope.launch(Dispatchers.IO) {

            val result = placesRepository.getCategoriesScore(placeType)

            result.onSuccess { responseBody ->
                val scoreJson = responseBody.toString()
                val gson = Gson()
                scoreCategoryResponse = gson.fromJson(scoreJson, ScoreCategoryResponse::class.java)

                Log.d("PlaceApi", "Resultado da consulta de pontuação por categoria é: ${result.toString()}")
            }.onFailure { exception ->
                Log.d("PlaceApi", "Error: ${exception.message}")
            }
        }
    }

    fun getAllPlaceInfoForCalculate(name : String, lat: String, long : String){
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val result = placesRepository.fetchPlaceByNameData(lat,long,name)

                result.onSuccess { responseBody ->

                    val infosPlaceJson = responseBody.toString()
                    val gson = Gson()
                    val parsedResponse = gson.fromJson(infosPlaceJson, PlaceInfo::class.java)

                    infosPlaceResponseForCalculate = parsedResponse
                    Log.d("PlacesApi", "Resultado da consulta das informações do lugar para calculo: $infosPlaceResponseForCalculate")
                }.onFailure { exception ->
                    Log.d("PlacesApi", "Error: ${exception.message}")
                }
            }
        }catch(e: Throwable){
            Log.d("PlacesApi", "Error: ${e}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkIsHoliday(events: List<EventsRequest>){
        try {
            val currentDate = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            events.forEach { event ->
                if(currentDate.format(formatter) == event.second){
                    isHoliday.value = true
                }else {
                    isHoliday.value = false
                }
            }
        }catch(e:Throwable){
        }
    }
}



