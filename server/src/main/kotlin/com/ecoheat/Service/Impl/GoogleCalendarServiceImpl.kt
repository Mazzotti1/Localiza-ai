package com.ecoheat.Service.Impl

import com.ecoheat.Apis.GoogleCalendarApi.GoogleCalendarApi
import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Model.DTOs.EventsData
import com.ecoheat.Model.DTOs.WeatherData
import com.ecoheat.Service.IGoogleCalendarService
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
class GoogleCalendarServiceImpl @Autowired constructor(private val messageSource: MessageSource): IGoogleCalendarService {
        val locale = Locale("pt")
        private lateinit var responseFromApi: Any
        private lateinit var future: CompletableFuture<Any>

    override fun getGoogleCalendarProps(localRequest: String?) {
        try {
            val googleCalendarApi = GoogleCalendarApi(null)
            future = CompletableFuture()
            googleCalendarApi.getEventsJson(localRequest,this)
        } catch(ex:RegistroIncorretoException){
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onGoogleCalendarFailure(errorMessage)
        }
    }

    fun getGoogleCalendarPropsCalc(localRequest: String?): CompletableFuture<List<Pair<String, String?>>> {
        val future = CompletableFuture<List<Pair<String, String?>>>()

        try {
            val googleCalendarApi = GoogleCalendarApi(null)
            googleCalendarApi.getEventsJson(localRequest, object : IGoogleCalendarService {
                override fun getGoogleCalendarProps(localRequest: String?) {
                    TODO("Not yet implemented")
                }

                override fun onGoogleCalendarResponse(response: List<Pair<String, String?>>) {
                    // Completa o futuro com a resposta obtida
                    future.complete(response)
                }

                override fun onGoogleCalendarFailure(error: String) {
                    // Completa o futuro com uma exceção em caso de erro
                    future.completeExceptionally(Exception(error))
                }
            })
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            future.completeExceptionally(Exception(errorMessage))
        }

        return future
    }

    override fun onGoogleCalendarResponse(response: List<Pair<String, String?>>) {
        responseFromApi = response
        future.complete(response)
    }

    override fun onGoogleCalendarFailure(error: String) {
        responseFromApi = error
        future.complete(error)
    }

    fun getApiResponse(): List<Pair<String, String?>> {
        return future.join() as List<Pair<String, String?>>
    }
}