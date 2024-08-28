package com.localizaai.Model

import java.sql.Timestamp

data class HistoryRequest(
    val name: String?,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val category: String,
    val type: String?,
    val updatedBy:Long
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
    val isActive : Boolean
)