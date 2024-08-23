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
    val historyId: Long,

    @Column(name="history_timestamp",nullable = false)
    val historyTimestamp: Timestamp,

    @ManyToOne
    @JoinColumn(name = "history_event", referencedColumnName = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "history_place", referencedColumnName = "place_id")
    val place: Place,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean
)
