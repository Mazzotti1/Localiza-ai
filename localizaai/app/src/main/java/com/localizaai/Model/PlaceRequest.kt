package com.localizaai.Model

import com.google.gson.annotations.SerializedName

data class PlaceRequest(
    @SerializedName("fsq_id") val fsqId: String,
    @SerializedName("name") val name: String
)

data class SpecificPlaceResponse(
    val fsq_id: String,
    val categories: List<Category>,
    val chains: List<Chain>,
    val closed_bucket: String,
    val geocodes: Geocodes,
    val link: String,
    val location: SpecifLocation,
    val name: String,
    val related_places: RelatedPlaces?,
    val timezone: String
)

data class Category(
    val id: Int,
    val name: String,
    val short_name: String,
    val plural_name: String,
    val icon: Icon
)

data class Icon(
    val prefix: String,
    val suffix: String
)

data class Chain(
    val id: String,
    val name: String
)

data class Geocodes(
    val drop_off: Coordinates,
    val main: Coordinates
)

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class SpecifLocation(
    val address: String,
    val country: String,
    val cross_street: String,
    val formatted_address: String,
    val locality: String,
    val postcode: String,
    val region: String
)

data class RelatedPlaces(
    val children: List<Children>,
)

data class Children(
    val fsqId: String,
    val categories: List<Category>,
    val name : String
)

data class PlaceInfo(
    val place: Place,
    val match_score: Double,
    val context: Context
)

data class Place(
    val fsq_id: String,
    val features: Features,
    val hours_popular: List<HoursPopular>,
    val name: String,
    val popularity: Double,
    val price: Int,
    val rating: Double,
    val social_media: SocialMedia,
    val stats: Stats,
    val tastes: List<String>,
    val tel: String,
    val timezone: String,
    val tips: List<Tip>,
    val verified: Boolean,
    val website: String
)

data class Features(
    val payment: Payment,
    val food_and_drink: FoodAndDrink,
    val services: Services,
    val amenities: Amenities,
    val attributes: Attributes
)

data class Payment(
    val credit_cards: CreditCards
)

data class CreditCards(
    val accepts_credit_cards: Boolean,
    val amex: Boolean,
    val visa: Boolean,
    val master_card: Boolean
)

data class FoodAndDrink(
    val alcohol: Alcohol,
    val meals: Meals
)

data class Alcohol(
    val cocktails: Boolean,
    val full_bar: Boolean
)

data class Meals(
    val brunch: Boolean,
    val happy_hour: Boolean,
    val dinner: Boolean
)

data class Services(
    val delivery: Boolean,
    val dine_in: DineIn
)

data class DineIn(
    val reservations: Boolean
)

data class Amenities(
    val restroom: Boolean,
    val smoking: Boolean,
    val live_music: Boolean,
    val private_room: Boolean,
    val outdoor_seating: Boolean,
    val tvs: Boolean,
    val parking: Parking,
    val wifi: String
)

data class Parking(
    val parking: Boolean,
    val private_lot: Boolean
)

data class Attributes(
    val clean: String,
    val crowded: String,
    val dates_popular: String,
    val families_popular: String,
    val groups_popular: String,
    val noisy: String,
    val quick_bite: String,
    val romantic: String,
    val service_quality: String,
    val special_occasion: String,
    val trendy: String,
    val value_for_money: String
)

data class HoursPopular(
    val close: String,
    val day: Int,
    val open: String
)

data class SocialMedia(
    val facebook_id: String
)

data class Stats(
    val total_photos: Int,
    val total_ratings: Int,
    val total_tips: Int
)

data class Tip(
    val created_at: String,
    val text: String
)

data class Context(
    val latitude: Double,
    val longitude: Double
)
