package com.ecoheat.Model

import jakarta.persistence.*
import java.awt.Point
import java.sql.Timestamp

@Entity
@Table(name="history")
data class History (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="history_id")
    val historyId: Long? = null,

    @Column(name="history_timestamp",nullable = false)
    val historyTimestamp: String,

    @Column(name = "entity_id", nullable = false)
    val entityId: Long,

    @Column(name = "entity_type", nullable = false)
    val entityType: String,

    @Column(name="latitude")
    var latitude:Double,

    @Column(name="longitude")
    var longitude:Double,

    @Column(name="updated_by")
    var updatedBy:Long,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean,

    @Column(name = "weather_id", nullable = false)
    var weatherId: Long,

    @Column(name = "traffic_id", nullable = false)
    var trafficId: Long
){
    constructor() : this(
        null,
        "",
        0L,
        "",
        0.0,
        0.0,
        0L,
        true,
        0L,
        0L
    )
}
