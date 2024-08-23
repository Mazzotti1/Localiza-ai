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

    override fun getHistory(): ApiResponse<List<History?>> {
        return try {
            val historyList = historyRepository.getAllHistory()

            if (historyList.isNotEmpty()) {
                ApiResponse(status = true, message = "Dados obtidos com sucesso", data = historyList)
            } else {
                ApiResponse(status = false, message = "Não há eventos ou lugares no histórico", data = historyList)
            }
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            throw RegistroIncorretoException(errorMessage)
        }
    }


    override fun getHistoryByid(id: Int?, type: String?): ApiResponse<History?> {
        return try {
            requireNotNull(id) { "ID must not be null" }
            requireNotNull(type) { "Type must not be null" }

            val historyItem = when (type) {
                "event" -> historyRepository.findHistoryByEventId(id)
                "place" -> historyRepository.findHistoryByPlaceId(id)
                else -> throw IllegalArgumentException("Invalid type: $type")
            }

            val historyLog = when (type){
                "event" -> "evento"
                "place" -> "lugar"
                else ->""
            }

            if (historyItem != null) {
                ApiResponse(status = true, message = "Dados obtidos com sucesso", data = historyItem)
            } else {
                ApiResponse(status = false, message = "Não há nenhum $historyLog no histórico", data = historyItem)
            }
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        } catch (ex: IllegalArgumentException) {
            val errorMessage = messageSource.getMessage("history.error.invalidType", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        }
    }


    override fun onHistoryResponse(response: ApiResponse<*>) {
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