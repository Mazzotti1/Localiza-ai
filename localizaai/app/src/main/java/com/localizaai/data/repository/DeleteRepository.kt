package com.localizaai.data.repository

import android.content.Context
import com.localizaai.Model.Delete
import com.localizaai.Model.Login
import com.localizaai.Model.Register
import com.localizaai.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class DeleteRepository(private val context: Context) {
    val sharedPreferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
    val authToken = sharedPreferences.getString("jwtToken", "").toString()
    private val apiService = NetworkClient.create(context, authToken)

    suspend fun deleteUser(userId: String): String {
        return try {
            withContext(Dispatchers.IO) {
                apiService.deleteUser(userId)
            }
            "Conta deletada com sucesso"

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
