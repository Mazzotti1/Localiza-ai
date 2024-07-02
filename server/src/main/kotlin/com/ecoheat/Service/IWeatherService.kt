package com.ecoheat.Service

interface IWeatherService {
    fun getWeatherProps(q: String?, days: Int?, hour: Int?, lang: String?)
    fun onWeatherResponse(response: String)
    fun onWeatherFailure(error: String)
}