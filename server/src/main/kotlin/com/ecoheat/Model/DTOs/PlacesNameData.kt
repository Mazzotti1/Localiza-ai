package com.ecoheat.Model.DTOs

data class PlacesNameData(
    val place: PlaceInfo? = null,
    val match_score: Double = 0.0,
    val context: Context? = null
)
class PlaceInfo (
    val fsq_id: String = "",
    val features: Features? = null,
    val hours_popular: List<HoursPopular> = emptyList(),
    val name: String = "",
    val popularity: Double = 0.0,
    val price: Int = 0,
    val rating: Double = 0.0,
    val social_media: SocialMedia? = null,
    val stats: Stats? = null,
    val tastes: List<String> = emptyList(),
    val tel: String = "",
    val timezone: String = "",
    val tips: List<Tip> = emptyList(),
    val verified: Boolean = false,
    val website: String = ""
)

