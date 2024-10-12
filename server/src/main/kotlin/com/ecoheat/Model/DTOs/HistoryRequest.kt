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


data class HistoryResponse(
    val status: Boolean,
    val message: String,
    val data: History
)

data class History(
    val historyId : Int,
    val historyTimestamp: String,
    val entityId : Int,
    val entityType: String,
    val latitude : Double,
    val longitude: Double,
    val updatedBy : Long,
    val isActive : Boolean,
    val weather: WeatherResponse,
    val traffic:TrafficResponse
)

data class WeatherResponse(
    val condition : String,
    val temperature : Double,
    val humidity : Int,
    val rainChance : Int
)

data class TrafficResponse(
    val currentSpeed : Int,
    val freeFlowSpeed : Int,
    val currentTravelTime : Int,
    val freeFlowTravelTime : Int,
    val confidence : Double,
    val roadClosure : Boolean,
)