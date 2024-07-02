package com.ecoheat.data.repository

import android.content.Context
import com.ecoheat.Model.Register
import com.ecoheat.data.remote.NetworkClient

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RegisterRepository(private val context: Context) {
    //caso precise tem que por o token como string na instancia da api
    private val apiService = NetworkClient.create(context)

    suspend fun registerUser(request: Register): String {
        return try {
            println("Usuario: $request")
            withContext(Dispatchers.IO) {
                apiService.registerUser(request)
            }
            "Conta criada com sucesso!"

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