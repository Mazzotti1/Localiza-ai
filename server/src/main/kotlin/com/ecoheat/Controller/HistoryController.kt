package com.ecoheat.Controller;

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Model.Category
import com.ecoheat.Model.DTOs.HistoryRequest
import com.ecoheat.Service.Impl.HistoryServiceImpl
import com.ecoheat.Service.Impl.WeatherServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Description
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp
import java.util.*

@RestController
@RequestMapping("/history")
class HistoryController(private val messageSource: MessageSource) {
    @Autowired
    private val historyService: HistoryServiceImpl? = null
    val locale = Locale("pt")

    @GetMapping
    fun getHistory (
        @RequestParam id: Long?,
    ): ResponseEntity<Any> {
        try {
            val responseFromApi = if (id != null) {
                 historyService!!.getHistoryByid(id)
            } else {
                 historyService!!.getHistory()
            }
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("history.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }

    }

    @GetMapping("location")
    fun getHistoryByLocation (
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam radius: String?
    ): ResponseEntity<Any> {
        try {
            val responseFromApi = if (radius != null) {
                  historyService!!.getHistoryByLocation(latitude,longitude, radius)
            }else {
                val errorMessage = messageSource.getMessage("history.error.request", null, locale)
                return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
            }
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("history.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/set")
    fun setHistory(
        @RequestBody parameters: HistoryRequest
    ) : ResponseEntity<Any>{
        try {
            val responseFromApi = when(parameters.type) {
                "place" -> historyService!!.setPlace(parameters)
                "event" -> historyService!!.setEvent(parameters)
                else -> throw IllegalArgumentException("Tipo inválido: $parameters.type")
            }

            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        }catch (ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("history.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }
}