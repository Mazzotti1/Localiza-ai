package com.ecoheat.Apis.Foursquare

import com.ecoheat.Model.DTOs.*
import com.ecoheat.Service.IFoursquareService
import com.ecoheat.Service.Impl.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.cdimascio.dotenv.dotenv
import okhttp3.*
import okhttp3.Response
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

        val categoryName = place.categories?.firstOrNull()?.name ?: ""
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val weatherData = weatherService?.getWeatherPropsCalc(place.location?.locality, 1, hour, language)?.get()
        val trafficData = trafficService?.getTomTomTrafficPropsCalc(place.geocodes?.main?.latitude, place.geocodes?.main?.longitude)?.get()
        val placeCategory = placeService?.getScoreCategoriesCalc(categoryName)?.get()
        val historyData = historyService?.getHistoryForPlace(place.fsq_id ?: "")?.get()

        val rawWeatherScore = getWeatherScore(weatherData, language)
        val rawTrafficScore = trafficData?.let { getTrafficScore(it) }
        var placeImpact = getPlaceScore(
            place.name ?: "",
            place.geocodes?.main?.latitude ?: 0.0,
            place.geocodes?.main?.longitude ?: 0.0,
            (place.closed_bucket ?: 0).toString()
        )

        val rawScores = listOf(rawWeatherScore, rawTrafficScore)
        val normalizedScores = normalizeScores(rawScores as List<Double>)

        val placeType = placeCategory?.type ?: "OUTDOOR"
        val placeWeight = placeCategory?.score ?: 1.0

        var weatherImpact = normalizedScores[0]
        var trafficImpact = normalizedScores[1]

        weatherImpact = adjustWeatherImpact(weatherImpact, weatherData, placeType, language)

        if (placeType == "INDOOR" && trafficImpact < 0.5) {
            placeImpact *= 1.2
        }

        var adjustedTrafficImpact = trafficImpact
        var adjustedWeatherImpact = weatherImpact
        var adjustedPlaceImpact = placeImpact

        if (historyData != null) {
            if(historyData.isNotEmpty()){
                val trafficTrends = getTrafficTrendsFromHistory(historyData) ?: 1.0
                val weatherTrends = getWeatherTrendsFromHistory(historyData) ?: 1.0
                val placeTrends = getPlacesTrendsFromHistory(historyData) ?: 1.0

                 adjustedTrafficImpact = trafficImpact * trafficTrends
                 adjustedWeatherImpact = weatherImpact * weatherTrends
                 adjustedPlaceImpact = placeImpact * placeTrends
            }
        }

        return calculateWeightedScore(adjustedPlaceImpact, adjustedWeatherImpact, adjustedTrafficImpact, placeWeight)
    }

    private fun calculateWeightedScore(placeImpact: Double, weatherImpact: Double, trafficImpact: Double, placeWeight: Double): Double {
        return placeWeight * (placeImpact + weatherImpact + trafficImpact) / 3
    }

    private fun adjustWeatherImpact(weatherImpact: Double, weatherData: WeatherData?, placeType: String, language: String): Double {
        var impact = weatherImpact
        val conditionText = if (language == "en") "Clear" else "Céu limpo"

        weatherData?.forecast?.forecastday?.forEach { weather ->
            weather.hour.forEach { hour ->
                val isDay = hour.is_day == 1
                if (placeType == "OUTDOOR") {
                    impact = adjustOutdoorWeatherImpact(impact, hour, isDay, conditionText)
                } else if (placeType == "INDOOR") {
                    impact = adjustIndoorWeatherImpact(impact, hour, isDay, conditionText)
                }
            }
        }
        return impact
    }

    private fun adjustOutdoorWeatherImpact(impact:Double, hour:Hour, isDay:Boolean, conditionText: String): Double {
        return when (isDay) {
            true -> { // Durante o dia
                if (hour.condition.text.contains(conditionText, ignoreCase = true)) {
                    impact * 1.2 // Aumenta o impacto se a condição for favorável
                } else {
                    impact * 0.6 // Reduz o impacto se a condição não for favorável
                }
            }
            else -> { // Durante a noite
                if (hour.condition.text.contains(conditionText, ignoreCase = true)) {
                    impact * 1.1 // Aumenta o impacto se a condição for favorável
                } else {
                    impact * 0.5 // Reduz o impacto se a condição não for favorável
                }
            }
        }
    }

    private fun adjustIndoorWeatherImpact(impact:Double, hour:Hour, isDay:Boolean, conditionText: String): Double{
        return when (isDay) {
            true -> { // Durante o dia
                if (hour.condition.text.contains(conditionText, ignoreCase = true)) {
                    impact * 1.2 // Aumenta o impacto se a condição for favorável
                } else {
                    impact * 0.8 // Reduz o impacto se a condição não for favorável
                }
            }
            else -> { // Durante a noite
                if (hour.condition.text.contains(conditionText, ignoreCase = true)) {
                    impact * 1.1 // Aumenta o impacto se a condição for favorável
                } else {
                    impact * 0.9 // Reduz o impacto se a condição não for favorável
                }
            }
        }
    }

    private fun getPlacesTrendsFromHistory(historyData:List<History>): Double{
        return 0.0
    }

    private fun getTrafficTrendsFromHistory(historyData: List<History>): Double {
        if (historyData.isEmpty()) return 1.0 // Retorna um padrão (neutro) se não houver dados

        // Extraindo as informações de tráfego
        val currentSpeeds = historyData.map { it.traffic.currentSpeed }
        val currentTravelTimes = historyData.map { it.traffic.currentTravelTime }

        val averageCurrentSpeed = currentSpeeds.average()
        val averageCurrentTravelTime = currentTravelTimes.average()

        val lastTraffic = historyData.last().traffic

        val speedTrend = when {
            lastTraffic.currentSpeed > averageCurrentSpeed -> 1.1 // Tendência de aumento de velocidade
            lastTraffic.currentSpeed < averageCurrentSpeed -> 0.9 // Tendência de diminuição de velocidade
            else -> 1.0 // Velocidade estável
        }

        val travelTimeTrend = when {
            lastTraffic.currentTravelTime < averageCurrentTravelTime -> 1.1 // Tendência de diminuição do tempo de viagem
            lastTraffic.currentTravelTime > averageCurrentTravelTime -> 0.9 // Tendência de aumento do tempo de viagem
            else -> 1.0 // Tempo de viagem estável
        }

        val closureImpact = if (lastTraffic.roadClosure) 0.8 else 1.0
        val confidenceImpact = if (lastTraffic.confidence < 0.5) 0.9 else 1.1

        val finalTrafficTrend = speedTrend * travelTimeTrend * closureImpact * confidenceImpact

        return finalTrafficTrend
    }


    private fun getWeatherTrendsFromHistory(historyData: List<History>): Double {
        if (historyData.isEmpty()) return 1.0

        val temperatures = historyData.map { it.weather.temperature.toDoubleOrNull() ?: 0.0 }

        val averageTemperature = temperatures.average()
        val currentTemperature = temperatures.lastOrNull() ?: 0.0

        val trend = when {
            currentTemperature > averageTemperature -> 1.1 // Tendência de aumento de temperatura
            currentTemperature < averageTemperature -> 0.9 // Tendência de diminuição de temperatura
            else -> 1.0 // Temperatura estável
        }

        // Ajuste com base na condição do tempo
        val conditionImpact = when {
            historyData.last().weather.condition.contains("Sunny", ignoreCase = true) -> 1.2
            historyData.last().weather.condition.contains("Rain", ignoreCase = true) -> 0.8
            else -> 1.0
        }

        return trend * conditionImpact
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