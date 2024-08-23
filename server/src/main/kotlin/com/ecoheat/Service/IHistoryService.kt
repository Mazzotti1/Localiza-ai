package com.ecoheat.Service

import com.ecoheat.Model.ApiResponse
import com.ecoheat.Model.History

interface IHistoryService {
    fun getHistory() : ApiResponse<List<History?>>
    fun getHistoryByid(id : Int?, type : String?) : ApiResponse<History?>
    fun onHistoryResponse(response: ApiResponse<*>)
    fun onHistoryFailure(error: String)
}
