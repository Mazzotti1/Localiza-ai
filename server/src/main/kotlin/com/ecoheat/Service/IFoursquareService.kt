package com.ecoheat.Service

import com.ecoheat.Apis.Foursquare.FoursquarePlace
import com.ecoheat.Model.ApiResponse
import com.ecoheat.Model.DTOs.ScoreTypeResponse

interface IFoursquareService {
    fun getPlacesId(lat: String,long: String, radius: String, sort: String)
    fun onPlacesResponse(response: List<FoursquarePlace>)
    fun onPlacesFailure(error: String)

    fun getSpecificPlace (id: String)
    fun onSpecificPlaceResponse(responseBody: String)

    fun getPlacesByName(lat: String,long: String,name: String)
    fun getPlacesTips(id: String)

    fun getAutocompletePlaces(search: String, lat: String, long: String)
    fun onAutocompletePlacesResponse(responseBody: String)

    fun setCategories()

    fun getScoreCategories(categoryType : String) : ScoreTypeResponse

    fun onScoreCategoriesResponse(responseBody: String)
}