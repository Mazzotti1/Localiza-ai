package com.localizaai.Model

import com.google.gson.annotations.SerializedName

data class PlaceRequest(
    @SerializedName("fsq_id") val fsqId: String,
    @SerializedName("name") val name: String
)

data class SpecificPlaceResponse(
    val fsq_id: String,
    val categories: List<Category> = emptyList(),
    val chains: List<Chain> = emptyList(),
    val closed_bucket: String = "",
    val geocodes: Geocodes? = null,
    val link: String = "",
    val location: SpecifLocation? = null,
    val name: String = "",
    val related_places: RelatedPlaces? = null,
    val timezone: String = "",
    val score : Double = 0.0,
    val type : String = ""
)

data class Category(
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
    val categories: List<Category> = emptyList(),
    val name: String = ""
)

data class PlaceInfo(
    val place: Place? = null,
    val match_score: Double = 0.0,
    val context: Context? = null
)

data class Place(
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

data class Features(
    val payment: Payment? = null,
    val food_and_drink: FoodAndDrink? = null,
    val services: Services? = null,
    val amenities: Amenities? = null,
    val attributes: Attributes? = null
)

data class Payment(
    val credit_cards: CreditCards? = null
)

data class CreditCards(
    val accepts_credit_cards: Boolean = false,
    val amex: Boolean = false,
    val visa: Boolean = false,
    val master_card: Boolean = false
)

data class FoodAndDrink(
    val alcohol: Alcohol? = null,
    val meals: Meals? = null
)

data class Alcohol(
    val cocktails: Boolean = false,
    val full_bar: Boolean = false
)

data class Meals(
    val brunch: Boolean = false,
    val happy_hour: Boolean = false,
    val dinner: Boolean = false
)

data class Services(
    val delivery: Boolean = false,
    val dine_in: DineIn? = null
)

data class DineIn(
    val reservations: Boolean = false
)

data class Amenities(
    val restroom: Boolean = false,
    val smoking: Boolean = false,
    val live_music: Boolean = false,
    val private_room: Boolean = false,
    val outdoor_seating: Boolean = false,
    val tvs: Boolean = false,
    val parking: Parking? = null,
    val wifi: String = ""
)

data class Parking(
    val parking: Boolean = false,
    val private_lot: Boolean = false
)

data class Attributes(
    val clean: String = "",
    val crowded: String = "",
    val dates_popular: String = "",
    val families_popular: String = "",
    val groups_popular: String = "",
    val noisy: String = "",
    val quick_bite: String = "",
    val romantic: String = "",
    val service_quality: String = "",
    val special_occasion: String = "",
    val trendy: String = "",
    val value_for_money: String = ""
)

data class HoursPopular(
    val close: String = "",
    val day: Int = 0,
    val open: String = ""
)

data class SocialMedia(
    val facebook_id: String = ""
)

data class Stats(
    val total_photos: Int = 0,
    val total_ratings: Int = 0,
    val total_tips: Int = 0
)

data class Tip(
    val created_at: String = "",
    val text: String = ""
)

data class Context(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class Autocomplete(
    val results: List<AutocompleteResult> = emptyList()
)

data class AutocompleteResult(
    val type : String?,
    val text : AutocompleteText,
    val place : AutocompletePlace?,
)

data class AutocompleteText(
    val primary: String?,
    val secondary: String?,
)

data class AutocompletePlace(
    val fsqId: String?,
    val categories: List<Category>,
    val distance: Int?,
    val geocodes: AutocompleteGeocodes?,
    val name: String?
)

data class AutocompleteGeocodes (
    val main: Coordinates? = null,
    val roof: Coordinates? = null,
    val drop_off: Coordinates?
)

data class ScoreCategoryResponse(
    val status: Boolean,
    val message: String,
    val data: CategoryData
)

data class CategoryData(
    val score: Double,
    val type: String
)

