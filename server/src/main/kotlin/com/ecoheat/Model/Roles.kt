package com.ecoheat.Model

import jakarta.persistence.*

@Entity
@Table(name = "roles")
data class Roles(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(nullable = false)
    val name: String
){
    constructor() : this(
        0L,
        ""
    )
}
