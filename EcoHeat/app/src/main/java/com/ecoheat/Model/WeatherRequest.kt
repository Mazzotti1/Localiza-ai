package com.ecoheat.Model

data class WeatherRequest(
    val cityName: String,
    val days: Int,
    val hour: Int,
    val lang: String
)
