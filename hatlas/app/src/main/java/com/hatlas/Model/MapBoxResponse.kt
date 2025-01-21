package com.hatlas.Model


data class SuggestionResponse(
    val suggestions: List<Suggestion>,
    val attribution: String,
    val response_id: String
)

data class Suggestion(
    val name: String,
    val mapbox_id: String,
    val feature_type: String,
    val place_formatted: String,
    val context: ContextMapBox,
    val language: String,
    val maki: String,
    val metadata: Map<String, Any>
)

data class ContextMapBox(
    val country: Country,
    val region: Region,
    val postcode: Postcode,
    val place: PlaceMapBox,
    val neighborhood: Neighborhood,
    val street: Street
)

data class Country(
    val id: String,
    val name: String,
    val country_code: String,
    val country_code_alpha_3: String
)

data class Region(
    val id: String,
    val name: String,
    val region_code: String,
    val region_code_full: String
)

data class Postcode(
    val id: String,
    val name: String
)

data class PlaceMapBox(
    val id: String,
    val name: String
)

data class Neighborhood(
    val id: String,
    val name: String
)

data class Street(
    val id: String,
    val name: String
)
///////////////////////////////////////////////
data class FeatureCollection(
    val type: String,
    val features: List<Feature>,
    val attribution: String,
    val response_id: String
)

data class Feature(
    val type: String,
    val geometry: Geometry,
    val properties: Properties
)

data class Geometry(
    val coordinates: List<Double>,
    val type: String
)

data class Properties(
    val name: String,
    val mapbox_id: String,
    val feature_type: String,
    val place_formatted: String,
    val context: ContextMapBox,
    val coordinates: Coordinates,
    val language: String,
    val maki: String,
    val metadata: Map<String, Any>
)

