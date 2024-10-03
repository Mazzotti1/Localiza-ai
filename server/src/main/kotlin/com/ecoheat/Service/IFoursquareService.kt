package com.ecoheat.Service

import com.ecoheat.Apis.Foursquare.FoursquarePlace
import com.ecoheat.Model.ApiResponse
import com.ecoheat.Model.DTOs.ScoreTypeResponse
import java.util.concurrent.CompletableFuture

interface IFoursquareService {
    fun getPlacesId(lat: String,long: String, radius: String, sort: String) : CompletableFuture<String>
    fun onPlacesResponse(response: List<FoursquarePlace>)
    fun onPlacesFailure(error: String)

    fun getSpecificPlace (id: String): CompletableFuture<String>
    fun onSpecificPlaceResponse(responseBody: String)

    fun getPlacesByName(lat: String,long: String,name: String) : CompletableFuture<String>
    fun getPlacesTips(id: String)  : CompletableFuture<String>

    fun getAutocompletePlaces(search: String, lat: String, long: String) : CompletableFuture<String>
    fun onAutocompletePlacesResponse(responseBody: String)

    fun setCategories()

    fun getScoreCategories(categoryType : String) : ScoreTypeResponse

    fun onScoreCategoriesResponse(responseBody: String)
}

abstract class BaseFoursquareService : IFoursquareService {
    override fun getPlacesId(lat: String, long: String, radius: String, sort: String): CompletableFuture<String> {
        throw UnsupportedOperationException("This method is not implemented yet.")
    }
    override fun onPlacesResponse(response: List<FoursquarePlace>) {}
    override fun onPlacesFailure(error: String) {}
    override fun getSpecificPlace(id: String): CompletableFuture<String> {
        throw UnsupportedOperationException("This method is not implemented yet.")
    }

    override fun onSpecificPlaceResponse(responseBody: String) {}
    override fun getPlacesByName(lat: String, long: String, name: String) : CompletableFuture<String> {
        throw UnsupportedOperationException("This method is not implemented yet.")
    }
    override fun getPlacesTips(id: String) : CompletableFuture<String> {
        throw UnsupportedOperationException("This method is not implemented yet.")
    }
    override fun getAutocompletePlaces(search: String, lat: String, long: String) : CompletableFuture<String> {
        throw UnsupportedOperationException("This method is not implemented yet.")
    }
    override fun onAutocompletePlacesResponse(responseBody: String) {}
    override fun setCategories() {}
    override fun getScoreCategories(categoryType: String): ScoreTypeResponse {
        return ScoreTypeResponse(0.0, "")
    }
    override fun onScoreCategoriesResponse(responseBody: String) {}
}
