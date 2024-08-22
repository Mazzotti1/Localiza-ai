package com.ecoheat.Model

import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="place")
data class Place (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val placeId: Long,

    @Column(nullable = false)
    val placeName: String,

    @Column(nullable = false)
    val placeDescription: String,

    @Column
    val placeLocation : String,

    @Column(nullable = false)
    val placeTimestamp: Timestamp,

    @Column(nullable = false)
    val placeCapacity: String,

    @ManyToOne
    @JoinColumn(name = "fk_place_category")
    val placeCategory: Category,

    ){
    companion object {
        const val DEFAULT_PLACE_CATEGORY_ID: Long = 8
    }
}
