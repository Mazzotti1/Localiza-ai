package com.ecoheat.Apis.TomTomTrafficApi

import com.ecoheat.Apis.GoogleCalendarApi.CalendarResponse
import com.ecoheat.Apis.GoogleCalendarApi.Event
import com.ecoheat.Apis.GoogleCalendarApi.EventDateTime
import com.ecoheat.Apis.GoogleCalendarApi.toDate
import com.ecoheat.Service.ITomTomTrafficService
import com.google.gson.Gson
import io.github.cdimascio.dotenv.dotenv
import okhttp3.*
import org.springframework.context.MessageSource
import java.io.IOException
import java.util.*

class TomTomTrafficApi (private val messageSource: MessageSource?) {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun getTrafficInfo(latitude: Double?, longitude: Double?, callback: ITomTomTrafficService){
        val locale = Locale("pt")
        val dotenv = dotenv()
        val apiKey = dotenv["TOMTOM_API_KEY"]!!

        val url = "https://api.tomtom.com/traffic/services/4/flowSegmentData/absolute/10/json?point=$latitude,$longitude&key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorCause = e.cause
                val errorMessage = e.message

                val formattedErrorMessage = "Ocorreu um erro ao chamar API: $errorMessage\nPorquê: $errorCause"
                callback.onTomTomTrafficFailure(messageSource!!.getMessage(formattedErrorMessage, null, locale))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val trafficResponse = gson.fromJson(responseBody, TomTomTrafficResponse::class.java)
                    val flowSegmentData = trafficResponse.flowSegmentData
                    val json = gson.toJson(flowSegmentData)
                    callback.onTomTomTrafficResponse(json)
                } else {
                    callback.onTomTomTrafficFailure(messageSource!!.getMessage("traffic.error.request", null, locale))
                }
            }
        })
    }
}

data class TomTomTrafficResponse(
    val flowSegmentData : FlowSegmentData
)

data class FlowSegmentData(
    val frc: String,
    val currentSpeed: Int,
    val freeFlowSpeed:Int,
    val currentTravelTime:Int,
    val freeFlowTravelTime:Int,
    val confidence: Int,
    val roadClosure:Boolean
)