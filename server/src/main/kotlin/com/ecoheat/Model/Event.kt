package com.ecoheat.Model

import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="event")
data class Event (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    val eventId: Long? = null,

    @Column(name="event_name",nullable = false)
    val eventName: String,

    @Column(name="event_description",nullable = false)
    val eventDescription: String,

    @Column(name="latitude")
    var latitude:Double,

    @Column(name="longitude")
    var longitude:Double,

    @Column(name="event_timestamp",nullable = false)
    val eventTimestamp: String,

    @JoinColumn(name = "category")
    val eventCategory: String,

    @Column(name="updated_by")
    var updatedBy:Long,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean
)
