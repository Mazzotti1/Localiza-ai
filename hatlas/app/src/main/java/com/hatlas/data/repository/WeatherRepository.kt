package com.hatlas.data.repository

import android.content.Context
import android.util.Log
import com.hatlas.data.remote.ApiService
import com.hatlas.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    private fun getApiService(): ApiService {
        return NetworkClient.create(context, getAuthToken())
    }

    private fun getAuthToken(): String {
        return sharedPreferences.getString("jwtToken", "") ?: ""
    }

    suspend fun fetchWeatherData(q: String, days: Int, hour: Int, lang: String): Result<String> {
        val apiService = getApiService()
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
