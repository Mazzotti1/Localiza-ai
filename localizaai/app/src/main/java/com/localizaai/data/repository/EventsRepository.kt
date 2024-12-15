package com.localizaai.data.repository

import android.content.Context
import android.util.Log
import com.localizaai.data.remote.ApiService
import com.localizaai.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventsRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)

    private fun getApiService(): ApiService {
        return NetworkClient.create(context, getAuthToken())
    }

    private fun getAuthToken(): String {
        return sharedPreferences.getString("jwtToken", "") ?: ""
    }

    suspend fun fetchEventsData(localRequest: String): Result<String> {
        val apiService = getApiService()
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getEventsData(localRequest)
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