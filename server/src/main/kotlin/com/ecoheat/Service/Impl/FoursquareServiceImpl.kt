package com.ecoheat.Service.Impl

import com.ecoheat.Apis.Foursquare.FoursquareApi
import com.ecoheat.Apis.Foursquare.FoursquarePlace
import com.ecoheat.Apis.GoogleCalendarApi.GoogleCalendarApi
import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Service.IFoursquareService
import com.ecoheat.Service.IGoogleCalendarService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
class FoursquareServiceImpl @Autowired constructor(private val messageSource: MessageSource): IFoursquareService {
    val locale = Locale("pt")
    private lateinit var responseFromApi: Any
    private lateinit var future: CompletableFuture<Any>

    override fun getPlacesId(lat: String,long: String, radius: String, sort: String) {
        try {
            val foursquareApi = FoursquareApi(null)
            future = CompletableFuture()
            foursquareApi.getPlacesId(lat,long,radius,sort,this)
        } catch(ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
    }

    override fun getSpecificPlace(id: String) {
        try{
            val foursquareApi = FoursquareApi(null)
            future = CompletableFuture()
            foursquareApi.getSpecificPlace(id,this)
        }catch(ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
    }

    override fun getPlacesByName(lat: String, long: String, name: String) {
        try {
            val foursquareApi = FoursquareApi(null)
            future = CompletableFuture()
            foursquareApi.getPlaceByName(lat,long,name,this)
        }catch(ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
    }

    override fun getPlacesTips(id: String) {
        try {
            val foursquareApi = FoursquareApi(null)
            future = CompletableFuture()
            foursquareApi.getPlacesTips(id,this)
        }catch(ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
    }
    override fun onPlacesResponse(response: List<FoursquarePlace>){
        responseFromApi = response
        future.complete(response)
    }
    override fun onSpecificPlaceResponse(responseBody: String) {
        responseFromApi = responseBody
        future.complete(responseBody)
    }

    override fun onPlacesFailure(error: String) {
        responseFromApi = error
        future.complete(error)
    }

    fun getApiResponse(): List<FoursquarePlace> {
        return future.join() as List<FoursquarePlace>
    }

    fun getSpecificApiResponse(): String{
        return future.join() as String
    }
}