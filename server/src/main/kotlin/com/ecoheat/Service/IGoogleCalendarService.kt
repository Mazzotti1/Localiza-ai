package com.ecoheat.Service

interface IGoogleCalendarService {

    fun getGoogleCalendarProps(localRequest: String?)
    fun onGoogleCalendarResponse(response: List<Pair<String, String?>>)
    fun onGoogleCalendarFailure(error: String)
}