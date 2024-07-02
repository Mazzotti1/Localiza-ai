package com.ecoheat.Controller

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Service.Impl.GoogleCalendarServiceImpl
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
@RequestMapping("/events")
class GoogleCalendarController(private val messageSource: MessageSource) {

    @Autowired
    private val googleCalendarService : GoogleCalendarServiceImpl? = null
    val locale = Locale("pt")

    @GetMapping
    fun getGoogleCalendarProps(
        @RequestParam localRequest: String?
    ): ResponseEntity<Any>{
        try {
            googleCalendarService!!.getGoogleCalendarProps(localRequest)
            val responseFromApi = googleCalendarService.getApiResponse()
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        }catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("weather.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }
}