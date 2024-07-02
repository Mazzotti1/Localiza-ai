package com.ecoheat.Service

interface ITomTomTrafficService {

    fun getTomTomTrafficProps(latitude: Double?, longitude: Double?)
    fun onTomTomTrafficResponse(response: String?)
    fun onTomTomTrafficFailure(error: String)
}