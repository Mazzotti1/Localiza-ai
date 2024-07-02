package com.ecoheat.Repository

import com.ecoheat.Model.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UsersRepository : JpaRepository<Users, Long> {
    fun getUserById(id: Long): Users?
    fun findByName(name: String?): Users?
    fun existsByName(name: String): Boolean
}