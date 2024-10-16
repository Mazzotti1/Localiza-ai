package com.ecoheat.Controller

import com.ecoheat.Apis.Foursquare.FoursquarePlace
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
import java.util.concurrent.CompletableFuture

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
    ): CompletableFuture<ResponseEntity<out Any>>? {
        return foursquareService?.getPlacesId(lat, long, radius, sort)
            ?.thenApply { responseFromApi ->
                try {
                    val processedResponse = foursquareService.getApiResponse(responseFromApi)
                    ResponseEntity(processedResponse, HttpStatus.ACCEPTED)
                } catch (ex: Exception) {
                    val errorMessage = messageSource.getMessage("place.error.request", null, locale)
                    ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
                }
            }
            ?.exceptionally { ex ->
                val errorMessage = messageSource.getMessage("place.error.request", null, locale)
                ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
            }
    }


    @GetMapping("/specific")
    fun getSpecificPlace(
        @RequestParam id: String,
        @RequestParam language: String
    ): CompletableFuture<ResponseEntity<String>>? {
        return foursquareService?.getSpecificPlace(id,language)
            ?.thenApply { responseFromApi ->
                val processedResponse = foursquareService.getSpecificApiPlaceResponse(responseFromApi)
                ResponseEntity(processedResponse, HttpStatus.ACCEPTED)
            }
            ?.exceptionally { ex ->
                val errorMessage = messageSource.getMessage("place.error.request", null, locale)
                ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
            }
    }

    @GetMapping("/byName")
    fun getPlaceByName(
        @RequestParam lat: String,
        @RequestParam long: String,
        @RequestParam name: String
    ): CompletableFuture<ResponseEntity<String>>? {
        return foursquareService?.getPlacesByName(lat,long,name)
            ?.thenApply { responseFromApi ->
                val processedResponse = foursquareService.getPlaceNameApiResponse(responseFromApi)
                ResponseEntity(processedResponse, HttpStatus.ACCEPTED)
            }
            ?.exceptionally { ex ->
                val errorMessage = messageSource.getMessage("place.error.request", null, locale)
                ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
            }
    }

    @GetMapping("/tips")
    fun getPlacesTips(
        @RequestParam id: String
    ): CompletableFuture<ResponseEntity<String>>? {
        return foursquareService?.getPlacesTips(id)
            ?.thenApply { responseFromApi ->
                val processedResponse = foursquareService.getTipsApiResponse(responseFromApi)
                ResponseEntity(processedResponse, HttpStatus.ACCEPTED)
            }
            ?.exceptionally { ex ->
                val errorMessage = messageSource.getMessage("place.error.request", null, locale)
                ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
            }
    }

    @GetMapping("/autocomplete")
    fun getAutocompletePlaces(
        @RequestParam search: String,
        @RequestParam lat: String,
        @RequestParam long: String,
    ): CompletableFuture<ResponseEntity<String>>? {
        return foursquareService?.getAutocompletePlaces(search,lat,long)
            ?.thenApply { responseFromApi ->
                val processedResponse = foursquareService.getAutocompletePlacesResponse(responseFromApi)
                ResponseEntity(processedResponse, HttpStatus.ACCEPTED)
            }
            ?.exceptionally { ex ->
                val errorMessage = messageSource.getMessage("place.error.request", null, locale)
                ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
            }
    }

    @GetMapping("/setCategories")
    fun setCategories(
    ): ResponseEntity<Any>{
       try {
           foursquareService!!.setCategories()
           val responseFromApi = foursquareService.getScoreCategoriesResponse()
           return ResponseEntity(responseFromApi, HttpStatus.ACCEPTED)
       } catch(ex: RegistroIncorretoException){
           val errorMessage = messageSource.getMessage("place.error.request", null, locale)
           return ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
       }
    }
    @GetMapping("/getScoreCategories")
    fun getScoreCategories(
        @RequestParam categoryType: String
    ): ResponseEntity<Any> {
        return try {
            val responseFromApi = foursquareService!!.getScoreCategories(categoryType)
            ResponseEntity(responseFromApi, HttpStatus.OK)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("place.error.request", null, locale)
            ResponseEntity(errorMessage, HttpStatus.BAD_REQUEST)
        }
    }


}   