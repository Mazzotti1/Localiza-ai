package com.ecoheat.Service.Impl

import com.ecoheat.Apis.TomTomTrafficApi.TomTomTrafficApi
import com.ecoheat.Apis.WeatherApi.WeatherApi
import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Model.DTOs.TrafficData
import com.ecoheat.Model.DTOs.WeatherData
import com.ecoheat.Model.Traffic
import com.ecoheat.Service.ITomTomTrafficService
import com.ecoheat.Service.IWeatherService
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
class TomTomTrafficService @Autowired constructor(private val messageSource: MessageSource?): ITomTomTrafficService {
    val locale = Locale("pt")
    private lateinit var responseFromApi: Any
    private lateinit var future: CompletableFuture<Any>

    override fun getTomTomTrafficProps(latitude: Double?, longitude: Double?) {
        try {
            val trafficApi = TomTomTrafficApi(null)

            future = CompletableFuture()
            trafficApi.getTrafficInfo(latitude,longitude, this)

        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource!!.getMessage("generic.service.error", null, locale)
            onTomTomTrafficFailure(errorMessage)
        }
    }

     fun getTomTomTrafficPropsCalc(latitude: Double?, longitude: Double?) : CompletableFuture<TrafficData> {
        val future = CompletableFuture<TrafficData>()
         try {
            val trafficApi = TomTomTrafficApi(null)
             trafficApi.getTrafficInfo(latitude,longitude, object : ITomTomTrafficService{
                 override fun getTomTomTrafficProps(latitude: Double?, longitude: Double?) {
                     TODO("Not yet implemented")
                 }

                 override fun onTomTomTrafficResponse(response: String?) {
                     val gson = Gson()
                     val trafficData = gson.fromJson(response, TrafficData::class.java)
                     future.complete(trafficData)
                 }

                 override fun onTomTomTrafficFailure(errorMessage: String) {
                     future.completeExceptionally(Exception(errorMessage))
                 }
             })

        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource!!.getMessage("generic.service.error", null, locale)
            onTomTomTrafficFailure(errorMessage)
        }
         return future
    }
    override fun onTomTomTrafficResponse(response: String?) {
        if (response != null) {
            responseFromApi = response
        }
        future.complete(response)
    }

    override fun onTomTomTrafficFailure(error: String) {
        responseFromApi = error
        future.complete(error)
    }

    fun getApiResponse(): Any {
        return future.join()
    }
}
