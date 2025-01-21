package com.hatlas.data.repository

import android.content.Context
import android.util.Log
import com.hatlas.Model.HistoryRequest
import com.hatlas.data.remote.ApiService
import com.hatlas.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRespository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    private fun getApiService(): ApiService {
        return NetworkClient.create(context, getAuthToken())
    }

    private fun getAuthToken(): String {
        return sharedPreferences.getString("jwtToken", "") ?: ""
    }

    suspend fun fetchHistoryData(id: String): Result<String> {
        val apiService = getApiService()
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getHistoryData(id)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Events"))
            }
        } catch (e: Exception) {
            Log.d("Events", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }

    suspend fun setHistoryData(request : HistoryRequest): Result<String> {
        val apiService = getApiService()
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.setHistory(request)
            }

             if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Events"))
            }

        } catch (e: Exception) {
            Log.d("Events", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }

    suspend fun fetchHistoryDataByLocation(lat: Double,long: Double,radius: String): Result<String> {
        val apiService = getApiService()
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getHistoryDataByLocation(lat,long,radius)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Events"))
            }
        } catch (e: Exception) {
            Log.d("Events", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }
}
