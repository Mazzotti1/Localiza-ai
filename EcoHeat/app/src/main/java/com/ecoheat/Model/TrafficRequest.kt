package com.ecoheat.Model

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
    val confidence : Int,
    val roadClosure : Boolean
)
