package com.ecoheat.Repository

import com.ecoheat.Model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.util.concurrent.CompletableFuture


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

    @Async
    @Query("""
    SELECT 
        h.history_id AS historyId, 
        h.history_timestamp AS historyTimestamp, 
        h.entity_id AS entityId, 
        h.entity_type AS entityType, 
        h.latitude AS latitude, 
        h.longitude AS longitude, 
        h.updated_by AS updatedBy, 
        h.is_active AS isActive, 
        w.condition AS weatherCondition, 
        w.temperature AS weatherTemperature, 
        w.humidity AS weatherHumidity, 
        w.rain_chance AS weatherRainChance, 
        t.current_speed AS trafficCurrentSpeed, 
        t.free_flow_speed AS trafficFreeFlowSpeed, 
        t.current_travel_time AS trafficCurrentTravelTime, 
        t.free_flow_travel_time AS trafficFreeFlowTravelTime, 
        t.confidence AS trafficConfidence, 
        t.road_closure AS trafficRoadClosure 
    FROM history h
    INNER JOIN place p ON p.place_id = h.entity_id AND p.is_active = true
    INNER JOIN traffic t ON t.traffic_id = h.traffic_id
    INNER JOIN weather w ON w.weather_id = h.weather_id
    WHERE h.is_active = true AND p.fsqid = :fsqId
    ORDER BY h.history_id DESC
    LIMIT 14
""", nativeQuery = true)
    fun findHistoryByFsqId(@Param("fsqId") fsqId: String): CompletableFuture<List<Array<Any>>>

}
interface EventRepository : JpaRepository<Event, Long>{
    @Query(value = """
    select count(e.event_id) 
        from event e 
    where e.event_name = :name
        and e.is_active = true
    """, nativeQuery = true)
    fun findEventByName(@Param("name") name: String) : Int
}
interface PlaceRepository : JpaRepository<Place, Long> {
    @Query(value = """
    select count(p.place_id) 
        from place p 
    where p.place_name = :name
        and p.is_active = true
    """, nativeQuery = true)
    fun findPlaceByName(@Param("name") name: String) : Int
}

interface WeatherRepository : JpaRepository<Weather, Long> {
}

interface TrafficRepository : JpaRepository<Traffic, Long> {
}

