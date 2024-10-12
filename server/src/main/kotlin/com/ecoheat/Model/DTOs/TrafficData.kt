package com.ecoheat.Model.DTOs

data class TrafficData (
    val frc : String,
    val currentSpeed : Int,
    val freeFlowSpeed : Int,
    val currentTravelTime : Int,
    val freeFlowTravelTime : Int,
    val confidence : Double,
    val roadClosure : Boolean
)
