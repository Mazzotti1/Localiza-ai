package com.ecoheat.Model.DTOs

data class SpecificPlaceData(
    val fsq_id: String? = null,
    val categories: List<Category>? = null,
    val chains: List<Chain>? = null,
    val closed_bucket: String? = null,
    val geocodes: Geocodes? = null,
    val link: String? = null,
    val location: SpecifLocation? = null,
    val name: String? = null,
    val related_places: RelatedPlaces? = null,
    val timezone: String? = null,
    var score: Double? = null,
    var categoryType: String? = null,
    val type : String? = null
)

data class Chain(
    val id: String = "",
    val name: String = ""
)

data class Geocodes(
    val drop_off: Coordinates? = null,
    val main: Coordinates? = null
)

data class Coordinates(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class SpecifLocation(
    val address: String = "",
    val country: String = "",
    val cross_street: String = "",
    val formatted_address: String = "",
    val locality: String = "",
    val postcode: String = "",
    val region: String = ""
)

data class RelatedPlaces(
    val children: List<Children> = emptyList()
)

data class Children(
    val fsqId: String = "",
    val categories: List<CategoryPlace> = emptyList(),
    val name: String = ""
)

data class CategoryPlace(
    val id: Int,
    val name: String = "",
    val short_name: String = "",
    val plural_name: String = "",
    val icon: Icon? = null
)
data class Icon(
    val prefix: String = "",
    val suffix: String = ""
)
