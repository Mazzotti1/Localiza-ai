package com.ecoheat.Model

import jakarta.persistence.*
@Entity
@Table(name = "eventCategories")
data class EventCategories(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    val type: String
)
