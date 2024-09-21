package com.ecoheat.Model.DTOs

data class FoursquareCategoriesResponse(
    val response: Response
)
data class Response(
    val categories: List<Category>
)

data class Category(
    val name: String,
    val categories: List<Subcategory>
)

data class Subcategory(
    val name: String,
    val categories: List<Subcategory>
)
