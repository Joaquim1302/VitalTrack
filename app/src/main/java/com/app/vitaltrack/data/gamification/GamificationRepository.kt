package com.app.vitaltrack.data.gamification

import android.content.Context
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class GamificationRepository(private val context: Context) {
    private val dataStore = GamificationDataStore(context)

    fun getGamificationStateFlow(clientId: Long) = dataStore.getGamificationStateFlow(clientId)

    suspend fun getGamificationState(clientId: Long): GamificationState {
        return dataStore.getGamificationState(clientId)
    }

    suspend fun registerEvent(event: GamificationEvent): GamificationResult {
        val clientId = when (event) {
            is GamificationEvent.MealRegistered -> event.clientId
            is GamificationEvent.FoodAdded -> event.clientId
            is GamificationEvent.WeightRegistered -> event.clientId
            is GamificationEvent.WorkoutRegistered -> event.clientId
            is GamificationEvent.CalorieGoalReached -> event.clientId
            is GamificationEvent.AppUsedToday -> event.clientId
        }

        val date = when (event) {
            is GamificationEvent.MealRegistered -> event.date
            is GamificationEvent.FoodAdded -> event.date
            is GamificationEvent.WeightRegistered -> event.date
            is GamificationEvent.WorkoutRegistered -> event.date
            is GamificationEvent.CalorieGoalReached -> event.date
            is GamificationEvent.AppUsedToday -> event.date
        }

        var state = dataStore.getGamificationState(clientId)
        var dailyState = dataStore.getDailyState(clientId, date)
        
        val messages = mutableListOf<String>()
        var pointsToAdd = 0
        val newlyUnlockedAchievements = mutableListOf<Achievement>()

        // 1. Process Event and Calculate Points
        when (event) {
            is GamificationEvent.MealRegistered -> {
                if (!dailyState.mealRegistered) {
                    pointsToAdd += GamificationRules.POINTS_FIRST_MEAL_OF_DAY
                    dailyState = dailyState.copy(mealRegistered = true)
                    messages.add("+${GamificationRules.POINTS_FIRST_MEAL_OF_DAY} pontos! Refeição registrada.")
                }
            }
            is GamificationEvent.FoodAdded -> {
                if (dailyState.foodsAddedCount < 5) {
                    pointsToAdd += GamificationRules.POINTS_FOOD_ADDED
                    dailyState = dailyState.copy(foodsAddedCount = dailyState.foodsAddedCount + 1)
                    messages.add("+${GamificationRules.POINTS_FOOD_ADDED} pontos! Alimento adicionado.")
                }
            }
            is GamificationEvent.WeightRegistered -> {
                if (!dailyState.weightRegistered) {
                    pointsToAdd += GamificationRules.POINTS_WEIGHT_REGISTERED
                    dailyState = dailyState.copy(weightRegistered = true)
                    messages.add("+${GamificationRules.POINTS_WEIGHT_REGISTERED} pontos! Peso registrado.")
                }
            }
            is GamificationEvent.WorkoutRegistered -> {
                if (!dailyState.workoutRegistered) {
                    pointsToAdd += GamificationRules.POINTS_WORKOUT_REGISTERED
                    dailyState = dailyState.copy(workoutRegistered = true)
                    messages.add("+${GamificationRules.POINTS_WORKOUT_REGISTERED} pontos! Treino registrado.")
                }
            }
            is GamificationEvent.CalorieGoalReached -> {
                if (!dailyState.calorieGoalReached) {
                    pointsToAdd += GamificationRules.POINTS_CALORIE_GOAL_REACHED
                    dailyState = dailyState.copy(calorieGoalReached = true)
                    messages.add("Meta batida! +${GamificationRules.POINTS_CALORIE_GOAL_REACHED} pontos.")
                }
            }
            is GamificationEvent.AppUsedToday -> {
                // Apenas para trigger de streak, sem pontos extras aqui se já ganhou por outras ações
            }
        }

        // 2. Update Streak
        val today = LocalDate.parse(date)
        val lastActiveStr = state.lastActiveDate
        
        if (lastActiveStr == null) {
            state = state.copy(currentStreak = 1, lastActiveDate = date)
        } else {
            val lastActive = LocalDate.parse(lastActiveStr)
            if (lastActive.isBefore(today)) {
                val daysBetween = ChronoUnit.DAYS.between(lastActive, today)
                if (daysBetween == 1L) {
                    state = state.copy(
                        currentStreak = state.currentStreak + 1,
                        lastActiveDate = date
                    )
                    pointsToAdd += GamificationRules.POINTS_DAILY_STREAK
                    messages.add("Sequência mantida: ${state.currentStreak} dias! +${GamificationRules.POINTS_DAILY_STREAK} pontos.")
                } else if (daysBetween > 1L) {
                    state = state.copy(currentStreak = 1, lastActiveDate = date)
                    messages.add("Nova sequência iniciada!")
                }
            }
        }
        state = state.copy(bestStreak = maxOf(state.bestStreak, state.currentStreak))

        // 3. Update Points and Level
        val oldLevel = state.level
        val newTotalPoints = state.totalPoints + pointsToAdd
        val newLevel = GamificationRules.calculateLevel(newTotalPoints)
        
        var levelUp = false
        if (newLevel > oldLevel) {
            levelUp = true
            messages.add("Você subiu para o nível $newLevel: ${GamificationRules.getLevelName(newLevel)}!")
        }

        state = state.copy(
            totalPoints = newTotalPoints,
            level = newLevel
        )

        // 4. Check Achievements
        val currentAchievements = state.unlockedAchievements.toMutableSet()
        
        GamificationRules.basicAchievements.forEach { achievement ->
            if (!currentAchievements.contains(achievement.id)) {
                val shouldUnlock = when (achievement.id) {
                    "FIRST_MEAL" -> dailyState.mealRegistered
                    "FIRST_WEIGHT" -> dailyState.weightRegistered
                    "FIRST_WORKOUT" -> dailyState.workoutRegistered
                    "THREE_DAY_STREAK" -> state.currentStreak >= 3
                    "SEVEN_DAY_STREAK" -> state.currentStreak >= 7
                    "CALORIE_GOAL_FIRST" -> dailyState.calorieGoalReached
                    else -> false
                }
                
                if (shouldUnlock) {
                    currentAchievements.add(achievement.id)
                    newlyUnlockedAchievements.add(achievement)
                    messages.add("Conquista desbloqueada: ${achievement.title}!")
                }
            }
        }
        
        state = state.copy(unlockedAchievements = currentAchievements)

        // 5. Save and Return
        dataStore.saveGamificationState(state)
        dataStore.saveDailyState(clientId, dailyState)

        return GamificationResult(
            pointsAdded = pointsToAdd,
            newTotalPoints = newTotalPoints,
            levelUp = levelUp,
            newLevel = newLevel,
            unlockedAchievements = newlyUnlockedAchievements,
            messages = messages
        )
    }
}
