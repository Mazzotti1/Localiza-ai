package com.ecoheat.Model.DTOs

import com.ecoheat.Model.Traffic
import com.ecoheat.Model.Weather
import java.sql.Timestamp

data class HistoryRequest(
    val historyTimestamp: String,
    val name : String,
    val description : String,
    val entityType: String,
    val latitude : Double,
    val longitude: Double,
    val category: String,
    val updatedBy : Long,
    val weather: Weather,
    val traffic:Traffic
)
