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

    @Column(name ="event_location" )
    val eventLocation : String,

    @Column(name="event_timestamp",nullable = false)
    val eventTimestamp: Timestamp,

    @Column(name="event_capacity",nullable = false)
    val eventCapacity: String,

    @ManyToOne
    @JoinColumn(name = "fk_event_category")
    val eventCategory: Category,

    @Column(name="updated_by")
    var updatedBy:Long,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean
){
    companion object {
        const val DEFAULT_EVENT_CATEGORY_ID: Long = 8
    }
}
