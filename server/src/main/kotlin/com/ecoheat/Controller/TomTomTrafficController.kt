package com.ecoheat.Controller

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Service.Impl.TomTomTrafficService
import com.ecoheat.Service.Impl.WeatherServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/traffic")
class TomTomTrafficController(private val messageSource: MessageSource) {
    @Autowired
    private val trafficService: TomTomTrafficService? = null
    val locale = Locale("pt")

    @GetMapping
    fun getTrafficInfo (
        @RequestParam latitude: Double?,
        @RequestParam longitude: Double?
    ): ResponseEntity<Any> {
        try {
            trafficService!!.getTomTomTrafficProps(latitude,longitude)
            val responseFromApi = trafficService.getApiResponse()
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("traffic.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }

    }
}

