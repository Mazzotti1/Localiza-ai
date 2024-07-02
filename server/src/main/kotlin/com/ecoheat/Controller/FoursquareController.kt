package com.ecoheat.Controller

import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Service.Impl.FoursquareServiceImpl
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
@RequestMapping("/places")
class FoursquareController (private val messageSource: MessageSource) {

    @Autowired
    private val foursquareService : FoursquareServiceImpl? = null
    val locale = Locale("pt")

    @GetMapping
    fun getPlaces(
        @RequestParam lat: String,
        @RequestParam long: String,
        @RequestParam radius: String,
        @RequestParam sort: String
    ): ResponseEntity<Any> {
        try {
            foursquareService!!.getPlacesId(lat,long,radius,sort)
            val responseFromApi = foursquareService.getApiResponse()
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        }catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("place.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/specific")
    fun getSpecificPlace(
        @RequestParam id: String,
    ): ResponseEntity<Any> {
        try {
            foursquareService!!.getSpecificPlace(id)
            val responseFromApi = foursquareService.getSpecificApiResponse()
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        }catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("place.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/byName")
    fun getPlaceByName(
        @RequestParam lat: String,
        @RequestParam long: String,
        @RequestParam name: String
    ): ResponseEntity<Any>{
        try {
            foursquareService!!.getPlacesByName(lat,long,name)
            val responseFromApi = foursquareService.getSpecificApiResponse()
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        }catch (ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("place.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }

    }

    @GetMapping("/tips")
    fun getPlacesTips(
        @RequestParam id: String
    ): ResponseEntity<Any>{
        try {
            foursquareService!!.getPlacesTips(id)
            val responseFromApi = foursquareService.getSpecificApiResponse()
            return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
        }catch (ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("place.error.request", null, locale)
            return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }

    }
}