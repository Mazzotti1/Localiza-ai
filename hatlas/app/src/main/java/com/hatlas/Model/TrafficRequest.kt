package com.hatlas.Model

data class TrafficRequest (
    val lat : Double,
    val long : Double
)

data class TrafficResponse (
    val frc : String,
    val currentSpeed : Int,
    val freeFlowSpeed : Int,
    val currentTravelTime : Int,
    val freeFlowTravelTime : Int,
    val confidence : Double,
    val roadClosure : Boolean
)
