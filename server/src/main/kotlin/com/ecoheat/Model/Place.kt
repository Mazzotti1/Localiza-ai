package com.ecoheat.Model

import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="place")
data class Place (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="place_id")
    val placeId: Long,

    @Column(name="place_name",nullable = false)
    val placeName: String,

    @Column(name="place_description",nullable = false)
    val placeDescription: String,

    @Column(name="place_location")
    val placeLocation : String,

    @Column(name="place_timestamp",nullable = false)
    val placeTimestamp: Timestamp,

    @Column(name="place_capacity",nullable = false)
    val placeCapacity: String,

    @ManyToOne
    @JoinColumn(name = "fk_place_category")
    val placeCategory: Category,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean
    ){
    companion object {
        const val DEFAULT_PLACE_CATEGORY_ID: Long = 8
    }
}
