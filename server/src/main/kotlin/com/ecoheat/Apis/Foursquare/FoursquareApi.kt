package com.ecoheat.Apis.Foursquare

import com.ecoheat.Model.DTOs.SpecificPlaceData
import com.ecoheat.Model.DTOs.TrafficData
import com.ecoheat.Model.DTOs.WeatherData
import com.ecoheat.Service.IFoursquareService
import com.ecoheat.Service.Impl.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.cdimascio.dotenv.dotenv
import okhttp3.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class FoursquareApi @Autowired constructor(
    private val messageSource: MessageSource?,
    private val weatherService: WeatherServiceImpl?,
    private val googleCalendarService: GoogleCalendarServiceImpl?,
    private val trafficService: TomTomTrafficService?,
    private val historyService: HistoryServiceImpl?,
    private val placeService : FoursquareServiceImpl?
) {
    val client = OkHttpClient()
    private val gson = Gson()
    val locale = Locale("pt")
    val dotenv = dotenv()
    val apiKey = dotenv["FOURSQUARE_API_KEY"]!!
    val apiCategoryKey = dotenv["FOURSQUARE_API_CATEGORY"]!!
    //sort can be = DISTANCE / RELEVANCE / RATING / POPULARITY

    private fun parseFoursquareResponse(jsonString: String): List<FoursquarePlace> {
        val response = gson.fromJson(jsonString, FoursquareResponse::class.java)
        return response.results
    }
    fun getPlacesId (lat: String, long:String, radius: String, sort: String ,callback: IFoursquareService){
        val url = "https://api.foursquare.com/v3/places/search?ll=${lat}%2C${long}&radius=${radius}&fields=name%2Cfsq_id%2Clocation&sort=${sort}"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val formattedErrorMessage = "Ocorreu um erro ao chamar API: ${e.message}\nPorquê: ${e.cause}"
                callback.onPlacesFailure(formattedErrorMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val results = parseFoursquareResponse(responseBody)
                    callback.onPlacesResponse(results)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    fun getSpecificPlace(id: String,language: String, callback: IFoursquareService) {
        val url = "https://api.foursquare.com/v3/places/$id"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val formattedErrorMessage = "Ocorreu um erro ao chamar API: ${e.message}\nPorquê: ${e.cause}"
                callback.onPlacesFailure(formattedErrorMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val gson = Gson()
                    val specificPlace = gson.fromJson(responseBody, SpecificPlaceData::class.java)

                    val placeScore = calculatePlaceMovementScore(specificPlace, language)

                    val updatedPlace = specificPlace.copy(
                        score = placeScore
                    )

                    val modifiedResponseBody = gson.toJson(updatedPlace)

                    callback.onSpecificPlaceResponse(modifiedResponseBody)
                } else {
                    callback.onPlacesFailure("Falha na requisição")
                }
            }
        })
    }

    fun getPlaceByName (lat: String, long:String, name: String ,callback: IFoursquareService){
        //para cada espaço usa %20
        val url =  "https://api.foursquare.com/v3/places/match?name=${name}&ll=${lat}%2C${long}&fields=fsq_id%2Cname%2Ctimezone%2Cdescription%2Ctel%2Cwebsite%2Csocial_media%2Cverified%2Chours_popular%2Crating%2Cstats%2Cpopularity%2Cprice%2Ctips%2Ctastes%2Cfeatures%2Cstore_id"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val formattedErrorMessage = "Ocorreu um erro ao chamar API: ${e.message}\nPorquê: ${e.cause}"
                callback.onPlacesFailure(formattedErrorMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSpecificPlaceResponse(responseBody)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    fun getPlacesTips (id: String ,callback: IFoursquareService){
        val url = "https://api.foursquare.com/v3/places/${id}/tips"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val formattedErrorMessage = "Ocorreu um erro ao chamar API: ${e.message}\nPorquê: ${e.cause}"
                callback.onPlacesFailure(formattedErrorMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSpecificPlaceResponse(responseBody)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    fun getAutocompletePlaces(search :String,lat: String,long: String, callback: IFoursquareService){
        val url = "https://api.foursquare.com/v3/autocomplete?query=${search}&ll=${lat}%2C${long}&radius=5000&limit=50"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val formattedErrorMessage = "Ocorreu um erro ao chamar API: ${e.message}\nPorquê: ${e.cause}"
                callback.onPlacesFailure(formattedErrorMessage)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onAutocompletePlacesResponse(responseBody)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    fun getAllSubcategories(callback: IFoursquareService) {
        val url = "https://api.foursquare.com/v2/venues/categories?v=20231010&oauth_token=$apiCategoryKey"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorMessage = "Ocorreu um erro ao chamar API: ${e.message}\nPorquê: ${e.cause}"
                callback.onPlacesFailure(messageSource!!.getMessage(errorMessage, null, locale))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onScoreCategoriesResponse(responseBody)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    private fun calculatePlaceMovementScore(place: SpecificPlaceData, language: String) : Double{

        var categoryName : String = ""
        val categories = place.categories
        if (categories!!.isNotEmpty()) {
            val firstCategory = categories[0]
            categoryName = firstCategory.name
        }

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val weatherData = weatherService!!.getWeatherPropsCalc(place.location!!.locality,1,hour,language).get()
        val trafficData = trafficService!!.getTomTomTrafficPropsCalc(place.geocodes!!.main!!.latitude,place.geocodes.main!!.longitude).get()
//        val historyData = historyService.getHistoryByLocation(latitude,longitude, "800")
        val placeCategory = placeService!!.getScoreCategoriesCalc(categoryName).get()

        val rawWeatherScore = getWeatherScore(weatherData, language)
        val rawTrafficScore = getTrafficScore(trafficData)
        val rawHistoryScore = getHistoryScore()
        var placeImpact = getPlaceScore(place.name.toString(),place.geocodes.main.latitude,place.geocodes.main.longitude, place.closed_bucket!!)


        val rawScores = listOf( rawWeatherScore, rawTrafficScore, rawHistoryScore)
        val normalizedScores = normalizeScores(rawScores)

        var placeType = ""
        if(placeCategory.type != ""){
            placeType = placeCategory.type
        }else {
            placeType = "OUTDOOR"
        }

        var placeWeight  = placeCategory.score

        var weatherImpact = normalizedScores[0]
        var trafficImpact = normalizedScores[1]
        val historyImpact = normalizedScores[2]

        var conditionText = when (language){
            "en" -> "Clear"
            "pt" -> "Céu limpo"
            else -> ""
        }

        if(placeType == "OUTDOOR"){
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

        } else if (placeType == "INDOOR"){
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

        val weightedScore = (
                (placeWeight * placeImpact) *
                (placeWeight * weatherImpact) *
                (placeWeight * trafficImpact) *
                (placeWeight * historyImpact)
                )

        return weightedScore
    }

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

    private fun getWeatherScore(weatherData: WeatherData?, language : String) : Double{
        var score: Double = 0.0
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
    private fun getTrafficScore(trafficData : TrafficData) : Double{
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
    private fun getHistoryScore() : Double{
        return 0.0
    }
    private fun getPlaceScore(name: String, latitude : Double, longitude: Double, closedBucket: String) : Double{
        var placeScore: Double = 0.0

        val infosPlace = placeService!!.getPlacesByNameCalc(latitude.toString(), longitude.toString(), name).get()

        try {
            val popularity = infosPlace!!.place!!.popularity
            when {
                popularity < 0.3 -> placeScore -= 0.4  // Baixíssima popularidade
                popularity < 0.5 -> placeScore -= 0.2  // Popularidade moderada
                popularity < 0.7 -> placeScore += 0.1  // Popularidade razoável
                else -> placeScore += 0.4
            }

            val rating = infosPlace!!.place!!.rating
            when {
                rating < 5.0 -> placeScore -= 0.5  // Rating muito baixo
                rating < 6.5 -> placeScore -= 0.2  // Rating baixo
                rating < 8.0 -> placeScore += 0.3  // Rating médio-alto
                else -> placeScore += 0.5
            }

            val verified = infosPlace!!.place!!.verified
            placeScore += if (verified == true) 0.2 else -0.1

            val matchScore = infosPlace!!.match_score
            when {
                matchScore < 0.4 -> placeScore -= 0.4  // Baixa correspondência
                matchScore < 0.7 -> placeScore -= 0.1  // Moderada
                matchScore < 0.9 -> placeScore += 0.2  // Boa
                else -> placeScore += 0.5
            }

            when (closedBucket) {
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

}

data class FoursquareResponse(val results: List<FoursquarePlace>)
data class FoursquarePlace(val fsq_id: String, val name: String)