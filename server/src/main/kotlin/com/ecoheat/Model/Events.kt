package com.ecoheat.Model

import jakarta.persistence.*
import java.awt.Point
import java.sql.Timestamp

@Entity
@Table(name="events")
data class Events (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val eventId: Long,

    @Column(nullable = false)
    val eventName: String,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = false)
    val eventTimestamp: Timestamp,

    @Column(nullable = false)
    val eventLocation: Point,

    @Column(nullable = false)
    val maxCapacity: Int,

    @ManyToOne
    @JoinColumn(name = "fk_eventcategories")
    val eventCategorie: EventCategories,

){
    companion object {
        const val DEFAULT_EVENT_CATEGORIE_ID: Long = 8
    }
}