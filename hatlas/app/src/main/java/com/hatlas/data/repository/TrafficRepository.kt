package com.hatlas.data.repository

import android.content.Context
import android.util.Log
import com.hatlas.data.remote.ApiService
import com.hatlas.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TrafficRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    private fun getApiService(): ApiService {
        return NetworkClient.create(context, getAuthToken())
    }

    private fun getAuthToken(): String {
        return sharedPreferences.getString("jwtToken", "") ?: ""
    }

    suspend fun fetchTrafficData(latitude: Double, longitude: Double): Result<String> {
        val apiService = getApiService()
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getTrafficData(latitude, longitude)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Traffic"))
            }
        } catch (e: Exception) {
            Log.d("Traffic", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }
}
