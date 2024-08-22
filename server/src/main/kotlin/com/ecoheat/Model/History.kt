package com.ecoheat.Model

import jakarta.persistence.*
import java.awt.Point
import java.sql.Timestamp

@Entity
@Table(name="history")
data class History (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val historyId: Long,

    @Column(nullable = false)
    val historyTimestamp: Timestamp,

    @ManyToOne
    @JoinColumn(name = "historyevent")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "historyPlace")
    val place: Place,

)
