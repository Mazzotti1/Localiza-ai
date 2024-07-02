package com.ecoheat.Controller

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Service.Impl.WeatherServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/weather")
class WeatherController(private val messageSource: MessageSource) {
    @Autowired
    private val weatherService: WeatherServiceImpl? = null
    val locale = Locale("pt")

    @GetMapping
    fun getWeatherProps (
        @RequestParam q: String?,
        @RequestParam days: Int?,
        @RequestParam hour: Int?,
        @RequestParam lang: String?
    ): ResponseEntity<Any> {
        try {
            weatherService!!.getWeatherProps(q, days, hour, lang)
            val responseFromApi = weatherService.getApiResponse()
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("weather.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }

    }
}