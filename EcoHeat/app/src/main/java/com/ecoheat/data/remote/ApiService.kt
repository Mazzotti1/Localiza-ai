package com.ecoheat.data.remote

import com.ecoheat.Model.Delete
import com.ecoheat.Model.Login
import com.ecoheat.Model.Register
import okhttp3.ResponseBody
import retrofit2.Call
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

    @GET("/weather") //implementar
    suspend fun getWeatherData(
        @Query("q") q: String,
        @Query("days") days: Int,
        @Query("hour") hour: Int,
        @Query("lang") lang: String
    ): Response<ResponseBody>

    @GET("/traffic") //implementar
    suspend fun getTrafficData(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Response<ResponseBody>


    @GET("/events") //implementar
    suspend fun getEventsData(@Query("localRequest") localRequest: String): Response<ResponseBody>

    @GET("/places") //implementar
    suspend fun getPlacesData(
        @Query("lat") lat: String,
        @Query("long") long: String,
        @Query("radius") radius: String,
        @Query("sort") sort: String
    ): Response<ResponseBody>


    @GET("/places/specific") //implementar
    suspend fun getSpecificPlaceData(@Query("id") id: String): Response<ResponseBody>

    @GET("/places/byName") //implementar
    suspend fun getPlacesByNameData(
        @Query("lat") lat: String,
        @Query("long") long: String,
        @Query("name") name : String
    ): Response<ResponseBody>

    @GET("/places/tips") //implementar
    suspend fun getPlaceTipsData(@Query("id") id: String): Response<ResponseBody>
}