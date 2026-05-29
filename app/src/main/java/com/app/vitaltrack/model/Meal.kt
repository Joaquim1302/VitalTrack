package com.app.vitaltrack.model

data class Meal(
    val id: Long,
    val name: String,
    val emoji: String,
    var calories: Double = 0.0
) {
    companion object {
        val defaultMeals = listOf(
            Meal(1, "Café da manhã", "🍳"),
            Meal(2, "Lanche", "🍎"),
            Meal(3, "Almoço", "🍲"),
            Meal(4, "Lanche da tarde", "🥪"),
            Meal(5, "Jantar", "🥗"),
            Meal(6, "Ceia", "🥛")
        )
    }
}
