package com.ecoheat.Model

import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="category")
data class Category (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    val categoryId: Long? = null,

    @Column(name="first_category",nullable = false)
    val firstCategory: String,

    @Column(name="second_category",nullable = false)
    val secondCategory: String,

    @Column(name="third_category",nullable = false)
    val thirdCategory: String,

    @Column(name="score", nullable = false)
    val score: Double,

    @Column(name="type", nullable = false)
    val type: String
){
    constructor() : this(
        0L,
        "",
        "",
        "",
        0.0,
        ""
    )
}


