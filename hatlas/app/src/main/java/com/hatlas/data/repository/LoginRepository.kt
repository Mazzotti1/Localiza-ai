package com.hatlas.data.repository

import android.content.Context
import com.hatlas.Model.Login
import com.hatlas.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class LoginResult(val token: String?, val message: String?)

class LoginRepository(private val context: Context) {
    private val apiService = NetworkClient.create(context)

    suspend fun loginUser(request: Login): LoginResult {
        return try {
            val response = withContext(Dispatchers.IO) {

                apiService.loginUser(request)
            }
            val token = response.string()
            LoginResult(token, "Logado com sucesso!")

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            println("Erro HTTP: $errorBody")
            LoginResult(null, errorBody ?: "HttpException")

        } catch (e: Throwable) {
            LoginResult(null, "Throwable")
        }
    }
}