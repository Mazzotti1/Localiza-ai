package com.ecoheat.Model

import jakarta.persistence.*
@Entity
@Table(name = "category")
data class Category(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val categoryId: Long,

    @Column(nullable = false)
    val type: String
)
