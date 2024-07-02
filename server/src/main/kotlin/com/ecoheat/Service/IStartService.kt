package com.ecoheat.Service

interface IStartService {
    fun getStartMessage(requestedMessage: String?): String?
}