package com.ecoheat.Model

import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="place")
data class Place (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="place_id")
    val placeId: Long? = null,

    @Column(name="place_name",nullable = false)
    val placeName: String,

    @Column(name="fsqid",nullable = false)
    val fsqId: String,

    @Column(name="place_description",nullable = false)
    val placeDescription: String,

    @Column(name="latitude")
    var latitude:Double,

    @Column(name="longitude")
    var longitude:Double,

    @Column(name="place_timestamp",nullable = false)
    val placeTimestamp: String,

    @JoinColumn(name = "category")
    val placeCategory: String,

    @Column(name="updated_by")
    var updatedBy:Long,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean
    )
