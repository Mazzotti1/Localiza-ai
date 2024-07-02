package com.ecoheat.Service.Impl

import com.ecoheat.Apis.GoogleCalendarApi.GoogleCalendarApi
import com.ecoheat.Exception.RegistroIncorretoException
import com.ecoheat.Service.IGoogleCalendarService
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