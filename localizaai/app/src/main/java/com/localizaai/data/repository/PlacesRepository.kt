package com.localizaai.data.repository

import android.content.Context
import android.util.Log
import com.localizaai.data.remote.ApiService
import com.localizaai.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlacesRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    private val apiService: ApiService

    init {
        apiService = NetworkClient.create(context, getAuthToken())
    }

    private fun getAuthToken(): String {
        val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("jwtToken", "") ?: ""
    }

    suspend fun fetchPlacesData(lat: String, long: String, radius: String, sort:String): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getPlacesData(lat, long, radius, sort)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Places"))
            }
        } catch (e: Exception) {
            Log.d("Places", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }

    suspend fun fetchSpecificPlacesData(id:String): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getSpecificPlaceData(id)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Specific Places"))
            }
        } catch (e: Exception) {
            Log.d("Places", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }

    suspend fun fetchPlaceByNameData(lat:String, long: String, name: String): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getPlacesByNameData(lat,long,name)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Places by name"))
            }
        } catch (e: Exception) {
            Log.d("Places", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }

    suspend fun fetchPlacesTipsData(id:String): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getPlaceTipsData(id)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Places Tips"))
            }
        } catch (e: Exception) {
            Log.d("Places", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }

    suspend fun fetchPlaceAutocomplete(search: String, lat:String, long: String): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getAutocompletePlaces(search,lat,long)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Places by name"))
            }
        } catch (e: Exception) {
            Log.d("Places", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }

    suspend fun getCategoriesScore(categoryType: String): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getCategoriesScore(categoryType)
            }

            if (response.isSuccessful) {
                val responseBody = response.body()?.string()
                Result.success(responseBody ?: "")
            } else {
                val errorBody = response.errorBody()?.string()
                Result.failure(Exception(errorBody ?: "Erro inesperado no Places by name"))
            }
        } catch (e: Exception) {
            Log.d("Places", "Erro ao fazer a requisição: $e")
            Result.failure(e)
        }
    }
}