package com.ecoheat.Model

import jakarta.persistence.*
import java.sql.Timestamp

@Entity
@Table(name="weather")
data class Weather (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="weather_id")
    val weatherId: Long? = null,

    @Column(name="condition", nullable = false)
    val condition : String,

    @Column(name="temperature", nullable = false)
    val temperature : String,

    @Column(name="humidity", nullable = false)
    val humidity : String,

    @Column(name="rain_chance", nullable = false)
    val rainChance : Int,
){
    constructor() : this(
        0L,
        "",
        "",
        "",
        0
    )
}

