package com.app.vitaltrack.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Search : Screen("search")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
    object MealRegistration : Screen("meal_registration/{date}/{typeId}") {
        fun createRoute(date: String, typeId: Int) = "meal_registration/$date/$typeId"
    }
}
