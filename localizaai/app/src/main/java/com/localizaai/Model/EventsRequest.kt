package com.localizaai.Model

data class Event(
    val name: String,
    val date: String
)

data class EventsRequest(
    val events: List<Event>
)