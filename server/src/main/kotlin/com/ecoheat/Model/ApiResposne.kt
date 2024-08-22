package com.ecoheat.Model

data class ApiResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T?
)
