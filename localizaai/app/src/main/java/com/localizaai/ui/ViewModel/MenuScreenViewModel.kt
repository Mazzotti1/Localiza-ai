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
import com.localizaai.Model.EventsRequest
import com.localizaai.Model.HistoryRequest
import com.localizaai.Model.HistoryResponse
import com.localizaai.Model.PlaceInfo
import com.localizaai.Model.PlaceRequest
import com.localizaai.Model.ScoreCategoryResponse
import com.localizaai.Model.SpecificPlaceResponse
import com.localizaai.Model.TrafficResponse
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
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

    var placesResponse by mutableStateOf<List<PlaceRequest>?>(null)
    var specificPlaceResponse by mutableStateOf<SpecificPlaceResponse?>(null)
    var specificPlaceList = mutableStateListOf<SpecificPlaceResponse>()
    var infosPlaceResponse by mutableStateOf<PlaceInfo?>(null)
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

    var maxEventScore = 0.4
    var maxWeatherScore = 0.9
    var maxTrafficScore = 0.4
    var maxHistoryScore = 0.8

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
        val days = 3
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

        val historyRequest = HistoryRequest(
            name = parsedResponse.name,
            description = parsedResponse.closed_bucket,
            latitude = parsedResponse.geocodes?.main?.latitude ?: 0.0,
            longitude = parsedResponse.geocodes?.main?.longitude ?: 0.0,
            timestamp = currentTimestamp,
            category = category,
            type = "place",
            updatedBy = userId.toLong()
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
        val eventData = eventsResponse
        val weatherData = weatherResponse
        val trafficData = trafficResponse
        val historyData = historyResponse

        val eventScore = normalizeScore(getEventScore(eventData), maxEventScore)
        val weatherScore = normalizeScore(getWeatherScore(weatherData), maxWeatherScore)
        val trafficScore = normalizeScore(getTrafficScore(trafficData), maxTrafficScore)
        val historyScore = normalizeScore(getHistoryScore(historyData), maxHistoryScore)

        var weight = 0.0
        weight = getWeightsForPlaceType(place)

        val weightedScore = (
            (weight * eventScore) +
            (weight * weatherScore) +
            (weight * trafficScore) +
            (weight * historyScore)
        )

        return weightedScore
    }

    private fun getEventScore(eventData:List<EventsRequest>): Double{
        return 0.0
    }

    private fun getWeatherScore(weatherData:WeatherResponse?):Double{
        return 0.0
    }

    private fun getTrafficScore(trafficData:TrafficResponse?):Double{
        return 0.0
    }

    private fun getHistoryScore(historyData:HistoryResponse?):Double{
        return 0.0
    }

    private fun getWeightsForPlaceType(place: SpecificPlaceResponse): Double {

        place.categories.forEach { category ->
            placeType = category.name
        }

        getScoreByCategory()

        if(scoreCategoryResponse!!.status){
            return scoreCategoryResponse!!.data
        }else {
            return 0.0
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isPeakTime(): Boolean {
        val currentHour = LocalTime.now().hour
        return currentHour in 11..13 || currentHour in 18..20
    }

    private fun normalizeScore(score: Double, maxScore: Double): Double {
        return score / maxScore
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

}



