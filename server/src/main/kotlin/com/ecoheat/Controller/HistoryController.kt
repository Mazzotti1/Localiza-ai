package com.ecoheat.Controller;

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Service.Impl.HistoryServiceImpl
import com.ecoheat.Service.Impl.WeatherServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/history")
class HistoryController(private val messageSource: MessageSource) {
    @Autowired
    private val historyService: HistoryServiceImpl? = null
    val locale = Locale("pt")

    @GetMapping
    fun getHistory (): ResponseEntity<Any> {
        try {
            historyService!!.getHistory()
            val responseFromApi = historyService.getApiResponse()
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("history.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }

    }
}