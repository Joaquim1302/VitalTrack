package com.app.vitaltrack.data.gamification

import kotlinx.serialization.Serializable

@Serializable
data class GamificationState(
    val clientId: Long,
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastActiveDate: String? = null,
    val level: Int = 1,
    val unlockedAchievements: Set<String> = emptySet()
)

@Serializable
data class DailyGamificationState(
    val date: String,
    val mealRegistered: Boolean = false,
    val foodsAddedCount: Int = 0,
    val allMealsRegistered: Boolean = false,
    val workoutRegistered: Boolean = false,
    val weightRegistered: Boolean = false,
    val calorieGoalReached: Boolean = false
)

data class GamificationResult(
    val pointsAdded: Int = 0,
    val newTotalPoints: Int = 0,
    val levelUp: Boolean = false,
    val newLevel: Int = 1,
    val unlockedAchievements: List<Achievement> = emptyList(),
    val messages: List<String> = emptyList()
)

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String
)

sealed class GamificationEvent {
    data class MealRegistered(
        val clientId: Long,
        val date: String,
        val mealType: String? = null
    ) : GamificationEvent()

    data class FoodAdded(
        val clientId: Long,
        val date: String
    ) : GamificationEvent()

    data class WeightRegistered(
        val clientId: Long,
        val date: String
    ) : GamificationEvent()

    data class WorkoutRegistered(
        val clientId: Long,
        val date: String
    ) : GamificationEvent()

    data class CalorieGoalReached(
        val clientId: Long,
        val date: String
    ) : GamificationEvent()

    data class AppUsedToday(
        val clientId: Long,
        val date: String
    ) : GamificationEvent()
}
