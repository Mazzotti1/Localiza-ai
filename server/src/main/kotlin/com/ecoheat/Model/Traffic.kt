package com.ecoheat.Model

import jakarta.persistence.*

@Entity
@Table(name="traffic")
data class Traffic (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="traffic_id")
    val trafficId: Long? = null,

    @Column(name="current_speed", nullable = false)
    val currentSpeed : Int,

    @Column(name="free_flow_speed", nullable = false)
    val freeFlowSpeed : Int,

    @Column(name="current_travel_time", nullable = false)
    val currentTravelTime : Int,

    @Column(name="free_flow_travel_time", nullable = false)
    val freeFlowTravelTime : Int,

    @Column(name="confidence", nullable = false)
    val confidence : Double,

    @Column(name="road_closure", nullable = false)
    val roadClosure : Boolean,
)
