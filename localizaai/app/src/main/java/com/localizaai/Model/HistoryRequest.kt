package com.localizaai.Model

import java.sql.Timestamp

data class HistoryRequest(
    val name: String?,
    val capacity: String?,
    val description: String?,
    val location: String?,
    val timestamp: Timestamp,
    val category: CategoryHistory,
    val type: String?,
    val updatedBy:Long
)

data class CategoryHistory (
    val categoryId : Long?,
    val type : String?
)
