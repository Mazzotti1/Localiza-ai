package com.ecoheat.Service.Impl

import com.ecoheat.Apis.Foursquare.FoursquareApi
import com.ecoheat.Apis.Foursquare.FoursquarePlace
import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Model.ApiResponse
import com.ecoheat.Model.DTOs.ScoreTypeResponse
import com.ecoheat.Repository.FoursquareRepository
import com.ecoheat.Service.BaseFoursquareService
import com.ecoheat.Service.IFoursquareService
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
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
    override fun getPlacesId(lat: String,long: String, radius: String, sort: String): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        try {
            val foursquareApi = FoursquareApi(null)

            foursquareApi.getPlacesId(lat,long,radius,sort, object : BaseFoursquareService() {
                override fun onPlacesResponse(response: List<FoursquarePlace>) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response)
                    future.complete(jsonResponse)
                }

                override fun onPlacesFailure(error: String) {
                    future.completeExceptionally(RegistroIncorretoException(error))
                }
            })

        } catch(ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
        return future
    }


    override fun getSpecificPlace(id: String): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        try {
            val foursquareApi = FoursquareApi(null)
            foursquareApi.getSpecificPlace(id, object : BaseFoursquareService() {
                override fun onSpecificPlaceResponse(responseBody: String) {
                    future.complete(responseBody)
                }

                override fun onPlacesFailure(error: String) {
                    future.completeExceptionally(RegistroIncorretoException(error))
                }
            })
        } catch (ex: Exception) {
            future.completeExceptionally(RegistroIncorretoException("generic.service.error"))
        }
        return future
    }



    override fun getPlacesByName(lat: String, long: String, name: String): CompletableFuture<String>  {
        val future = CompletableFuture<String>()
        try {
            val foursquareApi = FoursquareApi(null)

            foursquareApi.getPlaceByName(lat,long,name, object : BaseFoursquareService() {
                override fun onSpecificPlaceResponse(responseBody: String) {
                    future.complete(responseBody)
                }

                override fun onPlacesFailure(error: String) {
                    future.completeExceptionally(RegistroIncorretoException(error))
                }
            })
        }catch(ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
        return future
    }

    override fun getPlacesTips(id: String): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        try {
            val foursquareApi = FoursquareApi(null)

            foursquareApi.getPlacesTips(id, object : BaseFoursquareService() {
                override fun onSpecificPlaceResponse(responseBody: String) {
                    future.complete(responseBody)
                }

                override fun onPlacesFailure(error: String) {
                    future.completeExceptionally(RegistroIncorretoException(error))
                }
            })

        }catch(ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
        return future
    }

    //usado para setar no banco as categories do foursquare
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

    override fun getScoreCategories(categoryType: String) : ScoreTypeResponse {
        try {
            if (categoryType.isEmpty()) {
                return ScoreTypeResponse(score = 0.0, type = "")
            }

            val scoreDataList  = repository.getScoreByCategory(categoryType)

            val result = scoreDataList.map { scoreData ->
                val score = scoreData[0] as Double
                val type = scoreData[1] as String
                ScoreTypeResponse(score = score, type = type)
            }

            val data = ScoreTypeResponse(score = 0.0, type = "")
            for(value in result){
                data.score = value.score
                data.type = value.type
                break
            }

            if (data.score != 0.0) {
              return  data
            }else{
              return ScoreTypeResponse(score = 0.0, type = "")
            }

        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        }
        return ScoreTypeResponse(score = 0.0, type = "")
    }


    override fun getAutocompletePlaces(search: String, lat: String, long : String): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        try {
            val foursquareApi = FoursquareApi(null)
            foursquareApi.getAutocompletePlaces(search,lat, long, object : BaseFoursquareService() {
                override fun onAutocompletePlacesResponse(responseBody: String) {
                    future.complete(responseBody)
                }

                override fun onPlacesFailure(error: String) {
                    future.completeExceptionally(RegistroIncorretoException(error))
                }
            })
        }catch (ex: RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onPlacesFailure(errorMessage)
        }
        return future
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

    fun getApiResponse(response: String): List<FoursquarePlace> {
        val gson = Gson()
        return gson.fromJson(response, object : TypeToken<List<FoursquarePlace>>() {}.type)
    }


    fun getPlaceNameApiResponse(response: String): String {
        return response
    }

    fun getTipsApiResponse(response: String): String {
        return response
    }

    fun processJsonResponse(response: String): String {
        val gson = Gson()
        val jsonObject = gson.fromJson(response, JsonObject::class.java)

        val categoriesArray: JsonArray? = jsonObject.getAsJsonArray("categories")
        val categoryName: String = categoriesArray?.firstOrNull()?.asJsonObject
            ?.get("name")?.asString ?: ""

        val categorySpecs = getScoreCategories(categoryName)
        jsonObject.addProperty("score", categorySpecs.score)
        jsonObject.addProperty("type", categorySpecs.type)

        return gson.toJson(jsonObject)
    }

    fun getSpecificApiPlaceResponse(response: String): String {
        return processJsonResponse(response)
    }


    fun getAutocompletePlacesResponse(response: String): String {
        return response
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