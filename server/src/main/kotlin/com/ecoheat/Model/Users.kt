package com.ecoheat.Model

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class Users(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @ManyToOne
    @JoinColumn(name = "fk_role")
    val role: Roles,

    var token: String? = null,

    @Column(name = "isActive", nullable = false)
    var isActive: Boolean
)  {
    constructor() : this(
        0L,
        "",
        "",
        Roles(0, ""),
        null,
        true
    )

    companion object {
        const val DEFAULT_ROLE_ID: Long = 1
    }
}

