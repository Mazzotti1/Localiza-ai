package com.ecoheat.Model.DTOs

import com.ecoheat.Model.Category
import java.sql.Timestamp

data class HistoryRequest(
    val name: String?,
    val capacity: String?,
    val description: String?,
    val location: String?,
    val timestamp: Timestamp,
    val category: Category,
    val type: String?,
    val updatedBy:Long
)
