package com.ecoheat.Service

import com.ecoheat.Model.*
import com.ecoheat.Model.DTOs.HistoryRequest
import java.sql.Timestamp

interface IHistoryService {
    fun getHistory() : ApiResponse<List<History?>>
    fun getHistoryByid(id : Long?) : ApiResponse<History?>
    fun onHistoryResponse(response: ApiResponse<*>)
    fun onHistoryFailure(error: String)
    fun setEvent(parameters : HistoryRequest): ApiResponse<Any>
    fun setPlace(parameters : HistoryRequest): ApiResponse<Any>
}
