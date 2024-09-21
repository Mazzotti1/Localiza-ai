package com.ecoheat.Service.Impl

import com.ecoheat.Apis.Foursquare.FoursquareApi
import com.ecoheat.Apis.Foursquare.FoursquarePlace
import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Model.ApiResponse
import com.ecoheat.Model.Category
import com.ecoheat.Model.DTOs.FoursquareCategoriesResponse
import com.ecoheat.Model.DTOs.Subcategory
import com.ecoheat.Model.History
import com.ecoheat.Repository.FoursquareRepository
import com.ecoheat.Service.IFoursquareService
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture


@Service
class FoursquareServiceImpl @Autowired constructor(private val messageSource: MessageSource, private val repository: FoursquareRepository): IFoursquareService {
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

    override fun setCategories() {
        try {
            //service just to set categories from foursquare, if need in future just implement the object methods
//            val foursquareApi = FoursquareApi(null)
//            foursquareApi.getAllSubcategories(object : IFoursquareService {
//                override fun onScoreCategoriesResponse(responseBody: String) {
//                    val gson = Gson()
//                    val foursquareResponse: FoursquareCategoriesResponse = gson.fromJson(responseBody, FoursquareCategoriesResponse::class.java)
//
//                    for (category in foursquareResponse.response.categories) {
//                        val firstCategoryName = category.name
//
//                        for (secondCategory in category.categories) {
//                            val secondCategoryName = secondCategory.name
//
//                            for (thirdCategory in secondCategory.categories) {
//                                val thirdCategoryName = thirdCategory.name
//
//                                val categoryEntity = Category(
//                                    firstCategory = firstCategoryName,
//                                    secondCategory = secondCategoryName,
//                                    thirdCategory = thirdCategoryName
//                                )
//
//                                repository.save(categoryEntity)
//                            }
//                        }
//                    }
//
//                }
//            })
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
    }

    override fun getScoreCategories(categoryType: String): ApiResponse<Double> {
        return try {
            if (categoryType.isEmpty()) {
                return ApiResponse(status = false, message = "Tipo de categoria não pode ser vazio", data = null)
            }

            val score = repository.getScoreByCategory(categoryType) ?: 0.0

            if (score != 0.0) {
                ApiResponse(status = true, message = "Pontuação obtida com sucesso", data = score)
            } else {
                ApiResponse(status = false, message = "Nenhuma pontuação foi encontrada", data = score)
            }

        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        }
    }


    override fun getAutocompletePlaces(search: String, lat: String, long : String){
        try {
            val foursquareApi = FoursquareApi(null)
            future = CompletableFuture()
            foursquareApi.getAutocompletePlaces(search,lat, long, this)
        }catch (ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
    }

    override fun onAutocompletePlacesResponse(responseBody: String) {
        responseFromApi = responseBody
        future.complete(responseBody)
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
        val response = future.join()
        return if (response is List<*>) {
            @Suppress("UNCHECKED_CAST")
            response as List<FoursquarePlace>
        } else {
            throw IllegalStateException("Expected a List<FoursquarePlace> but received $response")
        }
    }

    fun getSpecificApiResponse(): String {
        val response = future.join()
        return if (response is String) {
            response
        } else  {
            throw IllegalStateException("Expected a String but received $response")
        }
    }

    fun getAutocompletePlacesResponse(): String {
        val response = future.join()
        return if (response is String) {
            response
        } else {
            throw IllegalStateException("Expected a String but received $response")
        }
    }

    override fun onScoreCategoriesResponse(responseBody: String) {
        responseFromApi = responseBody
        future.complete(responseBody)
    }

    fun getScoreCategoriesResponse() : String {
        val response = future.join()
        return if(response is String ){
            response
        } else {
            throw IllegalStateException("Expected a String but received $response")
        }
    }
}