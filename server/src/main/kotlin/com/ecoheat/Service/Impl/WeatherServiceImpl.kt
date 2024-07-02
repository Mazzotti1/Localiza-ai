package com.ecoheat.Service.Impl

import com.ecoheat.Exception.RegistroIncorretoException

import com.ecoheat.Service.IWeatherService
import com.ecoheat.Apis.WeatherApi.WeatherApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
class WeatherServiceImpl @Autowired constructor(private val messageSource: MessageSource): IWeatherService {
    val locale = Locale("pt")
    private lateinit var responseFromApi: Any
    private lateinit var future: CompletableFuture<Any>
    override fun getWeatherProps(q: String?, days: Int?, hour: Int?, lang: String?) {
        try {
            val weatherApi = WeatherApi(null)
            future = CompletableFuture()
            weatherApi.getWeatherJson(q, days, hour, lang, this)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onWeatherFailure(errorMessage)
        }
    }

    override fun onWeatherResponse(response: String) {
        responseFromApi = response
        future.complete(response)
    }

    override fun onWeatherFailure(error: String) {
        responseFromApi = error
        future.complete(error)
    }

    fun getApiResponse(): Any {
        return future.join()
    }
}