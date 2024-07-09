package com.localizaai.data.repository

import android.content.Context
import android.util.Log
import com.localizaai.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.HttpException

class WeatherRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    private val authToken = sharedPreferences.getString("jwtToken", "").toString()
    private val apiService = NetworkClient.create(context, authToken)

    suspend fun fetchWeatherData(q: String, days: Int, hour: Int, lang: String): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getWeatherData(q, days, hour, lang)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Weather"))
            }
        } catch (e: Exception) {
            Log.d("WeatherApi", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }
}
