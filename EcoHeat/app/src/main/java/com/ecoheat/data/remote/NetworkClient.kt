package com.ecoheat.data.remote

import android.content.Context
import com.ecoheat.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import io.github.cdimascio.dotenv.dotenv

object NetworkClient {


    fun create(context: Context, authToken: String? = null): ApiService {
        val baseUrl = context.getString(R.string.base_url)
        val okHttpClient = OkHttpClient.Builder()
            .apply {
                if (!authToken.isNullOrEmpty()) {
                    addInterceptor(AuthInterceptor().apply {
                        setToken(authToken)
                    })
                }
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}