package com.hatlas.data.repository

import android.content.Context
import android.util.Log
import com.hatlas.R
import com.hatlas.data.remote.ApiService
import com.hatlas.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MapboxRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    private val mapboxAuthToken = context.getString(R.string.mapbox_api)

    private fun getApiService(): ApiService {
        return NetworkClient.create(context, getAuthToken())
    }

    private fun getAuthToken(): String {
        return sharedPreferences.getString("jwtToken", "") ?: ""
    }


    suspend fun fetchMapBoxAutocompletes(query: String): Result<String> {
        val apiService = getApiService()
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getMapBoxAutocomplete(query,1,"[GENERATED-UUID]",mapboxAuthToken)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no MapBox autocomplete"))
            }
        } catch (e: Exception) {
            Log.d("MapBox", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }

    suspend fun fetchMapBoxPlaceData(query: String): Result<String> {
        val apiService = getApiService()
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getMapBoxSelectedData(query,1,mapboxAuthToken)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no MapBox selected place data"))
            }
        } catch (e: Exception) {
            Log.d("MapBox", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }
}