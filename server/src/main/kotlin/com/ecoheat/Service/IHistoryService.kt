package com.ecoheat.Service

import com.ecoheat.Model.ApiResponse
import com.ecoheat.Model.History

interface IHistoryService {
    fun getHistory()
    fun onHistoryResponse(response: ApiResponse<List<History?>>)
    fun onHistoryFailure(error: String)
}
