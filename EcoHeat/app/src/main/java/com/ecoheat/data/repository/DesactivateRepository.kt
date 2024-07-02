package com.ecoheat.data.repository

import android.content.Context
import com.ecoheat.Model.Delete
import com.ecoheat.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class DesactivateRepository(private val context: Context) {
    val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("jwtToken", "").toString()
    private val apiService = NetworkClient.create(context, authToken)

    suspend fun desactivateUser(userId: String): String {
        return try {
            withContext(Dispatchers.IO) {
                apiService.desactivateUser(userId)
            }
            "Conta desativada com sucesso"

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            println("Erro HTTP: $errorBody")
            errorBody ?: "HttpException"

        } catch (e: Throwable) {
            println(e)
            "Throwable"
        }
    }
}