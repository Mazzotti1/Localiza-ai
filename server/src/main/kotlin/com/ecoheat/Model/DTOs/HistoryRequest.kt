package com.ecoheat.Model.DTOs

import com.ecoheat.Model.Traffic
import com.ecoheat.Model.Weather


data class HistoryRequest(
    val historyTimestamp: String,
    val name : String,
    val fsqId : String,
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
    val historyId: Int,
    val historyTimestamp: String,
    val entityId: Int,
    val entityType: String,
    val latitude: Double,
    val longitude: Double,
    val updatedBy: Int,
    val isActive: Boolean,
    val weather: WeatherResponse,
    val traffic:TrafficResponse
)

data class WeatherResponse(
    val condition : String,
    val temperature : String,
    val humidity : String,
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