package com.localizaai.data.repository

import android.content.Context
import android.util.Log
import com.localizaai.Model.HistoryRequest
import com.localizaai.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRespository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    private val authToken = sharedPreferences.getString("jwtToken", "").toString()
    private val apiService = NetworkClient.create(context, authToken)

    suspend fun fetchHistoryData(id: String): Result<String> {
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
}
