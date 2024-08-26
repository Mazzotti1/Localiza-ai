package com.localizaai.Model

import java.sql.Timestamp

data class HistoryRequest(
    val name: String?,
    val capacity: String?,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Timestamp,
    val category: CategoryHistory,
    val type: String?,
    val updatedBy:Long
)

data class CategoryHistory (
    val categoryId : Long?,
    val type : String?
)


data class HistoryResponse(
    val status : Boolean,
    val message : String,
    val data : List<History>
)

data class History(
    val historyId : Int,
    val historyTimestamp: Timestamp,
    val entityId : Int,
    val latitude : Double,
    val longitude: Double,
    val updatedBy : Long,
    val isActive : Boolean
)