package com.ecoheat.Apis.Foursquare

import com.ecoheat.Service.IFoursquareService
import com.google.gson.Gson
import io.github.cdimascio.dotenv.dotenv
import okhttp3.*
import org.springframework.context.MessageSource
import java.io.IOException
import java.util.*

class FoursquareApi (private val messageSource: MessageSource?) {
    val client = OkHttpClient()
    private val gson = Gson()
    val locale = Locale("pt")
    val dotenv = dotenv()
    val apiKey = dotenv["FOURSQUARE_API_KEY"]!!
    val apiCategoryKey = dotenv["FOURSQUARE_API_CATEGORY"]!!
    //sort can be = DISTANCE / RELEVANCE / RATING / POPULARITY

    private fun parseFoursquareResponse(jsonString: String): List<FoursquarePlace> {
        val response = gson.fromJson(jsonString, FoursquareResponse::class.java)
        return response.results
    }
    fun getPlacesId (lat: String, long:String, radius: String, sort: String ,callback: IFoursquareService){
        val url = "https://api.foursquare.com/v3/places/search?ll=${lat}%2C${long}&radius=${radius}&fields=name%2Cfsq_id%2Clocation&sort=${sort}"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val results = parseFoursquareResponse(responseBody)
                    callback.onPlacesResponse(results)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    fun getSpecificPlace (id: String ,callback: IFoursquareService){
        val url = "https://api.foursquare.com/v3/places/${id}"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorCause = e.cause
                val errorMessage = e.message

                val formattedErrorMessage = "Ocorreu um erro ao chamar API: $errorMessage\nPorquê: $errorCause"
                callback.onPlacesFailure(messageSource!!.getMessage(formattedErrorMessage, null, locale))
            }

            override fun onResponse(call: Call, response: Response) {
                val categories = response.body
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSpecificPlaceResponse(responseBody)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    fun getPlaceByName (lat: String, long:String, name: String ,callback: IFoursquareService){
        //para cada espaço usa %20
        val url =  "https://api.foursquare.com/v3/places/match?name=${name}&ll=${lat}%2C${long}&fields=fsq_id%2Cname%2Ctimezone%2Cdescription%2Ctel%2Cwebsite%2Csocial_media%2Cverified%2Chours_popular%2Crating%2Cstats%2Cpopularity%2Cprice%2Ctips%2Ctastes%2Cfeatures%2Cstore_id"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorCause = e.cause
                val errorMessage = e.message

                val formattedErrorMessage = "Ocorreu um erro ao chamar API: $errorMessage\nPorquê: $errorCause"
                callback.onPlacesFailure(messageSource!!.getMessage(formattedErrorMessage, null, locale))

            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSpecificPlaceResponse(responseBody)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    fun getPlacesTips (id: String ,callback: IFoursquareService){
        val url = "https://api.foursquare.com/v3/places/${id}/tips"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorCause = e.cause
                val errorMessage = e.message

                val formattedErrorMessage = "Ocorreu um erro ao chamar API: $errorMessage\nPorquê: $errorCause"
                callback.onPlacesFailure(messageSource!!.getMessage(formattedErrorMessage, null, locale))

            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onSpecificPlaceResponse(responseBody)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    fun getAutocompletePlaces(search :String,lat: String,long: String, callback: IFoursquareService){
        val url = "https://api.foursquare.com/v3/autocomplete?query=${search}&ll=${lat}%2C${long}&radius=5000&limit=50"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .addHeader("Authorization", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorCause = e.cause
                val errorMessage = e.message

                val formattedErrorMessage = "Ocorreu um erro ao chamar API: $errorMessage\nPorquê: $errorCause"
                callback.onPlacesFailure(messageSource!!.getMessage(formattedErrorMessage, null, locale))

            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onAutocompletePlacesResponse(responseBody)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

    fun getAllSubcategories(callback: IFoursquareService) {
        val url = "https://api.foursquare.com/v2/venues/categories?v=20231010&oauth_token=$apiCategoryKey"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorMessage = "Ocorreu um erro ao chamar API: ${e.message}\nPorquê: ${e.cause}"
                callback.onPlacesFailure(messageSource!!.getMessage(errorMessage, null, locale))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onScoreCategoriesResponse(responseBody)
                } else {
                    callback.onPlacesFailure(messageSource!!.getMessage("place.error.request", null, locale))
                }
            }
        })
    }

}

data class FoursquareResponse(val results: List<FoursquarePlace>)
data class FoursquarePlace(val fsq_id: String, val name: String)