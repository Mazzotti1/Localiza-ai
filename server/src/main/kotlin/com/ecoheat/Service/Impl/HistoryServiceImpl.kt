package com.ecoheat.Service.Impl;

import com.ecoheat.Exception.RegistroIncorretoException

import com.ecoheat.Service.IWeatherService
import com.ecoheat.Apis.WeatherApi.WeatherApi
import com.ecoheat.Model.*
import com.ecoheat.Model.DTOs.HistoryRequest
import com.ecoheat.Repository.EventRepository
import com.ecoheat.Repository.HistoryRepository
import com.ecoheat.Repository.PlaceRepository
import com.ecoheat.Service.IHistoryService
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
    private val placeRepository: PlaceRepository
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

            var isDuplicatedEvent = parameters.name?.let { eventRepository.findEventByName(it) }
            if(isDuplicatedEvent != 0){
                return ApiResponse(status = false, message = "Lugar já existe", data = null)
            }

            if (parameters.name.isNullOrBlank() || parameters.description.isNullOrBlank() ) {
                throw IllegalArgumentException("Parâmetros obrigatórios não podem ser nulos ou vazios")
            }

            val newEvent = Event(null,parameters.name,parameters.description,parameters.latitude,parameters.longitude ,parameters.timestamp ,parameters.category ,parameters.updatedBy, true)
            val result = eventRepository.save(newEvent)

            setHistory(result.eventId, result.eventTimestamp, parameters.type,parameters.latitude,parameters.longitude, parameters.updatedBy)

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

            var isDuplicatedPlace = parameters.name?.let { placeRepository.findPlaceByName(it) }
            if(isDuplicatedPlace != 0){
                return ApiResponse(status = false, message = "Lugar já existe", data = null)
            }

            if (parameters.name.isNullOrBlank()  || parameters.description.isNullOrBlank() ) {
                throw IllegalArgumentException("Parâmetros obrigatórios não podem ser nulos ou vazios")
            }

            val newPlace = Place(null,parameters.name,parameters.description,parameters.latitude,parameters.longitude ,parameters.timestamp,parameters.category , parameters.updatedBy, true)
            val result = placeRepository.save(newPlace)

            setHistory(result.placeId, result.placeTimestamp, parameters.type,parameters.latitude,parameters.longitude, parameters.updatedBy)

        } catch (ex: IllegalArgumentException) {
            ApiResponse(status = false, message = ex.message ?: "Erro de parâmetro", data = null)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
            ApiResponse(status = false, message = errorMessage, data = null)
        }
    }
    fun setHistory(id: Long?, timestamp: Timestamp?, type: String?,latitude : Double, longitude: Double, updatedBy : Long): ApiResponse<Any> {
        return try {
            val historyLog = when (type) {
                "event" -> "evento"
                "place" -> "lugar"
                else -> throw IllegalArgumentException("Tipo inválido: $type")
            }

            val newHistory = History(null, timestamp!!, id!!,type, latitude,longitude ,updatedBy, true)
            historyRepository.save(newHistory)

            ApiResponse(status = true, message = "$historyLog criado com sucesso", data = newHistory)
        } catch (ex: RegistroIncorretoException) {
            val errorMessage = messageSource.getMessage("generic.service.error", null, locale)
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