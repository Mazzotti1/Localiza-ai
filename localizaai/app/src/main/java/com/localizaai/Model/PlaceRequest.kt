package com.localizaai.Model

import com.google.gson.annotations.SerializedName

data class PlaceRequest(
    @SerializedName("fsq_id") val fsqId: String,
    @SerializedName("name") val name: String
)