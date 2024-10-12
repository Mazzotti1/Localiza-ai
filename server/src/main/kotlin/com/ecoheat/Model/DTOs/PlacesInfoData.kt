package com.ecoheat.Model.DTOs

data class PlaceInfoData(
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
