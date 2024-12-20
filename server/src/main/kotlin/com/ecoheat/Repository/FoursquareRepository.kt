package com.ecoheat.Repository

import com.ecoheat.Model.Category
import com.ecoheat.Model.DTOs.ScoreTypeResponse
import com.ecoheat.Model.History
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface FoursquareRepository : JpaRepository<Category, Long> {

    @Query(value = """
    select 
	    c.score,
        c.type
    from category c 
    where c.first_category = :categoryType
        or c.second_category = :categoryType
        or c.third_category = :categoryType
    """, nativeQuery = true)
    fun getScoreByCategory(@Param("categoryType") categoryType: String): List<Array<Any>>

    @Query(value = """
    select distinct
	    c.first_category 
    from category c 
    where 
        c.second_category = :categoryName
        or
        c.third_category = :categoryName
    """, nativeQuery = true)
    fun getCategoryType(@Param("categoryName") categoryName: String) : Optional<String>
}

