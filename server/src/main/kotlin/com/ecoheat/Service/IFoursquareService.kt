package com.ecoheat.Service

import com.ecoheat.Apis.Foursquare.FoursquarePlace

interface IFoursquareService {
    fun getPlacesId(lat: String,long: String, radius: String, sort: String)
    fun onPlacesResponse(response: List<FoursquarePlace>)
    fun onPlacesFailure(error: String)

    fun getSpecificPlace (id: String)
    fun onSpecificPlaceResponse(responseBody: String)

    fun getPlacesByName(lat: String,long: String,name: String)
    fun getPlacesTips(id: String)
}