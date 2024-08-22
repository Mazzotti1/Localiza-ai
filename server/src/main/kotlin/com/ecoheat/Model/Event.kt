package com.ecoheat.Model

import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="event")
data class Event (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val eventId: Long,

    @Column(nullable = false)
    val eventName: String,

    @Column(nullable = false)
    val eventDescription: String,

    @Column
    val eventLocation : String,

    @Column(nullable = false)
    val eventTimestamp: Timestamp,

    @Column(nullable = false)
    val eventCapacity: String,

    @ManyToOne
    @JoinColumn(name = "fk_event_category")
    val eventCategory: Category,

){
    companion object {
        const val DEFAULT_EVENT_CATEGORY_ID: Long = 8
    }
}
