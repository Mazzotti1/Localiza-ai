package com.ecoheat.Apis.GoogleCalendarApi
import com.ecoheat.Service.IGoogleCalendarService
import com.google.gson.Gson
import io.github.cdimascio.dotenv.dotenv
import okhttp3.*
import org.springframework.context.MessageSource
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
class GoogleCalendarApi(private val messageSource: MessageSource?) {
    val client = OkHttpClient()
    private val gson = Gson()

    fun getEventsJson(localRequest: String?, callback: IGoogleCalendarService) {
        val locale = Locale("pt")
        val dotenv = dotenv()
        val apiKey = dotenv["GOOGLE_API_KEY"]!!
        val client = OkHttpClient()

        val publicCalendarId = "$localRequest%23holiday@group.v.calendar.google.com"
        val url = "https://www.googleapis.com/calendar/v3/calendars/$publicCalendarId/events?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val errorCause = e.cause
                val errorMessage = e.message

                val formattedErrorMessage = "Ocorreu um erro ao chamar API: $errorMessage\nPorquê: $errorCause"
                callback.onGoogleCalendarFailure(messageSource!!.getMessage(formattedErrorMessage, null, locale))
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val calendarResponse = gson.fromJson(responseBody, CalendarResponse::class.java)

                    val currentDate = Calendar.getInstance().time
                    val nextThreeEvents = calendarResponse.items
                        .filter { it.start.date?.toDate()?.after(currentDate) ?: false }
                        .sortedBy { it.start.date?.toDate() }
                        .take(3)
                        .map { Pair(it.summary, it.start.date) }

                    callback.onGoogleCalendarResponse(nextThreeEvents)
                } else {
                    callback.onGoogleCalendarFailure(messageSource!!.getMessage("weather.error.request", null, locale))
                }
            }
        })
    }
}

data class CalendarResponse(val items: List<Event>)
data class Event(
    val summary: String,
    val start: EventDateTime,
    val end: EventDateTime
)


data class EventDateTime(
    val date: String? = null,
)
fun String.toDate(): Date {
    return SimpleDateFormat("yyyy-MM-dd").parse(this)
}