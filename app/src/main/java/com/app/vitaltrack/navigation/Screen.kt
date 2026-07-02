package com.app.vitaltrack.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Search : Screen("search")
    object Progress : Screen("progress")
    object Settings : Screen("settings")
    object Profile : Screen("profile")
    object TransferData : Screen("transfer_data")
    object Gamification : Screen("gamification")
    object GymWorkout : Screen("gym_workout")
    object TreinoMarkdownImport : Screen("treino_markdown_import?uri={uri}") {
        fun createRoute(uri: String?) = if (uri != null) "treino_markdown_import?uri=${android.net.Uri.encode(uri)}" else "treino_markdown_import"
    }
    object WorkoutExecution : Screen("workout_execution/{cdSessao}") {
        fun createRoute(cdSessao: Long) = "workout_execution/$cdSessao"
    }
    object MealRegistration : Screen("meal_registration/{date}/{typeId}") {
        fun createRoute(date: String, typeId: Int) = "meal_registration/$date/$typeId"
    }
}
