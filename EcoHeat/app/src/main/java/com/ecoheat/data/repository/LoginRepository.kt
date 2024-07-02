package com.ecoheat.data.repository

import android.content.Context
import com.ecoheat.Model.Login
import com.ecoheat.Model.Register
import com.ecoheat.data.remote.NetworkClient
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
            println("Erro servidor")
            LoginResult(null, "Throwable")
        }
    }
}