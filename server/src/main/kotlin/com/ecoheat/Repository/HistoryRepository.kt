package com.ecoheat.Repository

import com.ecoheat.Model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp


@Repository
interface HistoryRepository : JpaRepository<History, Long> {
    fun findAllByIsActive(isActive: Boolean): List<History>
    fun findByHistoryIdAndIsActive(historyId: Long, isActive: Boolean): History?
    @Query(value = """
    SELECT h.*, ST_DistanceSphere(
        ST_MakePoint(CAST(:long AS double precision), CAST(:lat AS double precision)),
        ST_MakePoint(h.longitude, h.latitude)) AS distance
    FROM history h
    WHERE ST_DistanceSphere(
            ST_MakePoint(CAST(:long AS double precision), CAST(:lat AS double precision)),
            ST_MakePoint(h.longitude, h.latitude)) <= :radius
        AND h.is_active = true;
    """, nativeQuery = true)
    fun findByHistoryLocation(@Param("lat") lat: Double, @Param("long") long: Double, @Param("radius") radius: Double): List<History>




}
interface EventRepository : JpaRepository<Event, Long>
interface PlaceRepository : JpaRepository<Place, Long>

