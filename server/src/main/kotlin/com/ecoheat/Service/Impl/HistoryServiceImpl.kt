package com.ecoheat.Service.Impl;

import com.ecoheat.Exception.RegistroIncorretoException

import com.ecoheat.Service.IWeatherService
import com.ecoheat.Apis.WeatherApi.WeatherApi
import com.ecoheat.Model.*
import com.ecoheat.Model.DTOs.HistoryRequest
import com.ecoheat.Model.DTOs.History as DTOHistory
import com.ecoheat.Model.DTOs.TrafficResponse
import com.ecoheat.Model.DTOs.WeatherData
import com.ecoheat.Model.DTOs.WeatherResponse
import com.ecoheat.Repository.*
import com.ecoheat.Service.IHistoryService
import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.parsing.Location
import org.springframework.context.MessageSource
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.CompletableFuture

@Service
class HistoryServiceImpl @Autowired
constructor(
    private val messageSource: MessageSource,
    private val historyRepository: HistoryRepository,
    private val eventRepository: EventRepository,
    private val placeRepository: PlaceRepository,
    private val weatherRepository: WeatherRepository,
    private val trafficRepository: TrafficRepository
): IHistoryService {

    val locale = Locale("pt")
    private lateinit var responseFromApi: Any
    private lateinit var future: CompletableFuture<Any>

    override fun getHistory(): ApiResponse<List<History?>> {
        return try {

            val historyList: List<History> = historyRepository.findAllByIsActive(true)

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


    override fun getHistoryByid(id: Long?): ApiResponse<History?> {
        return try {
            requireNotNull(id) { "ID must not be null" }

            val historyItem = historyRepository.findByHistoryIdAndIsActive(id, true)

            if (historyItem != null) {
                ApiResponse(status = true, message = "Dados obtidos com sucesso", data = historyItem)
            } else {
                ApiResponse(status = false, message = "Não há nenhum dado no histórico para o id $id", data = historyItem)
            }
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        } catch (ex: IllegalArgumentException) {
            val errorMessage = messageSource.getMessage("history.error.invalidType", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        }
    }

    override fun getHistoryByLocation(latitude: Double, longitude: Double, radius: String): ApiResponse<List<History?>> {
        return try {

            val historyList: List<History> = historyRepository.findByHistoryLocation(latitude,longitude,radius.toDouble())

            if (historyList.isNotEmpty()) {
                ApiResponse(status = true, message = "Dados obtidos com sucesso", data = historyList)
            } else {
                ApiResponse(status = false, message = "Não há nenhum dado no histórico para a location ", data = historyList)
            }
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        } catch (ex: IllegalArgumentException) {
            val errorMessage = messageSource.getMessage("history.error.invalidType", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        }
    }

    override fun setEvent(
        parameters : HistoryRequest
    ): ApiResponse<Any> {
        return try {

            if (parameters.name.isNullOrBlank()  || parameters.description.isNullOrBlank() ) {
                throw IllegalArgumentException("Parâmetros obrigatórios não podem ser nulos ou vazios")
            }

            val newEvent = Event(null, parameters.name, parameters.description,parameters.latitude,parameters.longitude ,parameters.historyTimestamp, parameters.category, parameters.updatedBy, true)
            val result = eventRepository.save(newEvent)

            val weather = parameters.weather
            val weatherResult = weatherRepository.save(weather)

            val traffic = parameters.traffic
            val trafficResult = trafficRepository.save(traffic)

            setHistory(result.eventId, result.eventTimestamp, parameters.entityType,parameters.latitude,parameters.longitude, parameters.updatedBy, weatherResult.weatherId!!, trafficResult.trafficId!!)

        } catch (ex: IllegalArgumentException) {
            ApiResponse(status = false, message = ex.message ?: "Erro de parâmetro", data = null)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        }
    }

    override fun setPlace(
        parameters : HistoryRequest
    ): ApiResponse<Any> {
        return try {

            if (parameters.name.isNullOrBlank()  || parameters.description.isNullOrBlank() ) {
                throw IllegalArgumentException("Parâmetros obrigatórios não podem ser nulos ou vazios")
            }

            val newPlace = Place(null,parameters.name,parameters.fsqId,parameters.description,parameters.latitude,parameters.longitude ,parameters.historyTimestamp,parameters.category , parameters.updatedBy, true)
            val result = placeRepository.save(newPlace)

            val weather = parameters.weather
            val weatherResult = weatherRepository.save(weather)

            val traffic = parameters.traffic
            val trafficResult = trafficRepository.save(traffic)

            setHistory(result.placeId, result.placeTimestamp, parameters.entityType,parameters.latitude,parameters.longitude, parameters.updatedBy, weatherResult.weatherId!!, trafficResult.trafficId!!)

        } catch (ex: IllegalArgumentException) {
            ApiResponse(status = false, message = ex.message ?: "Erro de parâmetro", data = null)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        }
    }
    fun setHistory(id: Long?, timestamp: String?, type: String?,latitude : Double, longitude: Double, updatedBy : Long,weatherId : Long, trafficId: Long): ApiResponse<Any> {
        return try {
            val historyLog = when (type) {
                "event" -> "evento"
                "place" -> "lugar"
                else -> throw IllegalArgumentException("Tipo inválido: $type")
            }

            val newHistory = History(null, timestamp!!, id!!,type, latitude,longitude ,updatedBy, true, weatherId, trafficId)
            historyRepository.save(newHistory)

            ApiResponse(status = true, message = "$historyLog criado com sucesso", data = newHistory)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        }
    }

    fun getHistoryForPlace(fsqId: String): CompletableFuture<List<DTOHistory>> {
        val future = CompletableFuture<List<DTOHistory>>()

        val historyFuture = historyRepository.findHistoryByFsqId(fsqId)

        historyFuture.thenApply { result ->
            val historyList = result.map { row ->
                val weatherResponse = WeatherResponse(
                    condition = row[8] as String,
                    temperature = row[9] as String,
                    humidity = row[10] as String,
                    rainChance = row[11] as Int
                )

                val trafficResponse = TrafficResponse(
                    currentSpeed = (row[12] as? Long)?.toInt() ?: 0,  
                    freeFlowSpeed = (row[13] as? Long)?.toInt() ?: 0,  
                    currentTravelTime = (row[14] as? Long)?.toInt() ?: 0,  
                    freeFlowTravelTime = (row[15] as? Long)?.toInt() ?: 0,  
                    confidence = row[16] as Double,
                    roadClosure = row[17] as Boolean
                )

                DTOHistory(
                    historyId = (row[0] as? Long)?.toInt() ?: 0,  
                    historyTimestamp = row[1] as String,
                    entityId = (row[2] as? Long)?.toInt() ?: 0,  
                    entityType = row[3] as String,
                    latitude = row[4] as Double,
                    longitude = row[5] as Double,
                    updatedBy = (row[6] as? Long)?.toInt() ?: 0,  
                    isActive = row[7] as Boolean,
                    weather = weatherResponse,
                    traffic = trafficResponse
                )
            }

            future.complete(historyList)
        }.exceptionally { ex ->
            future.completeExceptionally(ex)
            null
        }

        return future
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

}