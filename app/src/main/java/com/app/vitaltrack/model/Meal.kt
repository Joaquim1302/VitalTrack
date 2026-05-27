package com.app.vitaltrack.model

data class Meal(
    val id: Long,
    val name: String,
    val emoji: String,
    var calories: Double = 0.0
)
