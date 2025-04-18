package com.hatlas.data.remote

import com.hatlas.Model.HistoryRequest
import com.hatlas.Model.Login
import com.hatlas.Model.Register
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @POST("/users/register")
    suspend fun registerUser(@Body request: Register): ResponseBody

    @POST("/users/login")
    suspend fun loginUser(@Body request: Login): ResponseBody

    @DELETE("/users/delete/{userId}")
    suspend fun deleteUser(@Path("userId") userId: String): ResponseBody

    @DELETE("/users/desactivate/{userId}")
    suspend fun desactivateUser(@Path("userId") userId: String): ResponseBody

    @GET("/weather")
    suspend fun getWeatherData(
        @Query("q") q: String,
        @Query("days") days: Int,
        @Query("hour") hour: Int,
        @Query("lang") lang: String
    ): Response<ResponseBody>

    @GET("/traffic")
    suspend fun getTrafficData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<ResponseBody>


    @GET("/events")
    suspend fun getEventsData(@Query("localRequest") localRequest: String): Response<ResponseBody>

    @GET("/places")
    suspend fun getPlacesData(
        @Query("lat") lat: String,
        @Query("long") long: String,
        @Query("radius") radius: String,
        @Query("sort") sort: String
    ): Response<ResponseBody>


    @GET("/places/specific")
    suspend fun getSpecificPlaceData(@Query("id") id: String, @Query("language") language: String): Response<ResponseBody>

    @GET("/places/byName")
    suspend fun getPlacesByNameData(
        @Query("lat") lat: String,
        @Query("long") long: String,
        @Query("name") name : String
    ): Response<ResponseBody>

    @GET("/places/tips")
    suspend fun getPlaceTipsData(@Query("id") id: String): Response<ResponseBody>

    @GET("/places/autocomplete")
    suspend fun getAutocompletePlaces(
        @Query("search") search : String,
        @Query("lat") lat: String,
        @Query("long") long: String
    ): Response<ResponseBody>

    @GET("/history/{userId}")
    suspend fun getHistoryData (@Path("id") id: String): Response<ResponseBody>

    @POST("/history/set")
    suspend fun setHistory(@Body request: HistoryRequest): Response<ResponseBody>

    @GET("/history/location")
    suspend fun getHistoryDataByLocation(
        @Query("latitude") lat: Double,
        @Query("longitude") long: Double,
        @Query("radius") radius: String
    ): Response<ResponseBody>

    @GET("/places/getScoreCategories")
    suspend fun getCategoriesScore(
        @Query("categoryType") categoryType : String
    ) : Response<ResponseBody>

    @GET("https://api.mapbox.com/search/searchbox/v1/suggest")
    suspend fun getMapBoxAutocomplete(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("session_token") sessionToken: String,
        @Query("access_token") apiToken: String
    ): Response<ResponseBody>

    @GET("https://api.mapbox.com/search/searchbox/v1/forward")
    suspend fun getMapBoxSelectedData(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("access_token") apiToken: String
    ) : Response<ResponseBody>

}

