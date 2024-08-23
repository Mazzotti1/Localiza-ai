package com.ecoheat.Repository

import com.ecoheat.Model.ApiResponse
import com.ecoheat.Model.History
import com.ecoheat.Model.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface HistoryRepository : JpaRepository<History, Long> {
    @Query("SELECT h FROM History h where h.isActive = TRUE")
    fun getAllHistory(): List<History?>

    @Query("""
        SELECT h
        FROM History h
        INNER JOIN h.event e
        WHERE h.isActive = TRUE
        AND e.eventId = :eventId
        AND e.isActive = TRUE
    """)
    fun findHistoryByEventId(@Param("eventId") eventId: Int): History?

    @Query("""
        SELECT h
        FROM History h
        INNER JOIN h.place p
        WHERE h.isActive = TRUE
        AND p.placeId = :placeId
        AND p.isActive = TRUE
    """)
    fun findHistoryByPlaceId(@Param("placeId") placeId: Int): History?
}