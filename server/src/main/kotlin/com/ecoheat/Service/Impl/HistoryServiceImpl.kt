package com.ecoheat.Service.Impl;

import com.ecoheat.Exception.RegistroIncorretoException

import com.ecoheat.Service.IWeatherService
import com.ecoheat.Apis.WeatherApi.WeatherApi
import com.ecoheat.Model.ApiResponse
import com.ecoheat.Model.History
import com.ecoheat.Repository.HistoryRepository
import com.ecoheat.Service.IHistoryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
class HistoryServiceImpl @Autowired
constructor(
    private val messageSource: MessageSource,
    private val historyRepository: HistoryRepository
): IHistoryService {

    val locale = Locale("pt")
    private lateinit var responseFromApi: Any
    private lateinit var future: CompletableFuture<Any>

    override fun getHistory() {
        try {
            val apiResponse: ApiResponse<List<History?>>
            future = CompletableFuture()
            val historyList = historyRepository.getAllHistory()
            if(historyList.isNotEmpty()){
                 apiResponse = ApiResponse(status = true, message = "Dados obtidos com sucesso", data = historyList)
            } else {
                 apiResponse = ApiResponse(status = false, message = "Não há eventos ou lugares no histórico", data = historyList)
            }

            onHistoryResponse(apiResponse)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            onHistoryFailure(errorMessage)
        }
    }

    override fun onHistoryResponse(response: ApiResponse<List<History?>>) {
        responseFromApi = response
        future.complete(response)
    }

    override fun onHistoryFailure(error: String) {
        val apiResponse = ApiResponse<List<History?>>(status = false, message = error, data = null)
        responseFromApi = apiResponse
        future.complete(apiResponse)
    }


    fun getApiResponse(): Any {
        return future.join()
    }
}