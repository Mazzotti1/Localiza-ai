package com.ecoheat.Repository

import com.ecoheat.Model.ApiResponse
import com.ecoheat.Model.History
import com.ecoheat.Model.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface HistoryRepository : JpaRepository<History, Long> {
    @Query("SELECT h FROM History h")
    fun getAllHistory(): List<History?>

}