package com.ecoheat.Model.DTOs

import com.ecoheat.Model.Traffic
import com.ecoheat.Model.Weather
import java.sql.Timestamp

data class HistoryRequest(
    val name: String?,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Timestamp,
    val category: String,
    val type: String?,
    val updatedBy:Long,
    val weather: Weather,
    val traffic:Traffic
)
