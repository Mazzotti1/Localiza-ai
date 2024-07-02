package com.ecoheat.Apis.WeatherApi
import com.ecoheat.Service.IWeatherService
import io.github.cdimascio.dotenv.dotenv
import okhttp3.*
import org.springframework.context.MessageSource
import java.io.IOException
import java.util.*


class WeatherApi(private val messageSource: MessageSource?) {
    fun getWeatherJson(q: String?, days: Int?, hour: Int?, lang: String?, callback: IWeatherService) {

        val locale = Locale("pt")
        val dotenv = dotenv()
        val apiKey = dotenv["WEATHER_API_KEY"]!!
        val client = OkHttpClient()

        val url = "https://api.weatherapi.com/v1/forecast.json?key=$apiKey&q=$q&days=$days&hour=$hour&lang=$lang"
        val request = Request.Builder()
            .url(url)
            .header("x-api-key", apiKey)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorCause = e.cause
                val errorMessage = e.message

                val formattedErrorMessage = "Ocorreu um erro ao chamar API: $errorMessage\nPorquê: $errorCause"
                callback.onWeatherFailure(messageSource!!.getMessage(formattedErrorMessage, null, locale))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    callback.onWeatherResponse(responseBody)
                } else {
                    callback.onWeatherFailure(messageSource!!.getMessage("weather.error.request", null, locale))
                }
            }
        })
    }
}
