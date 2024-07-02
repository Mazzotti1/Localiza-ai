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
    @JoinColumn(name = "fk_event")
    val event: Events,

    @Column(nullable = false)
    val metereological: String,

    @Column(nullable = false)
    val people: Int
)