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
                val mealType = event.mealType ?: "Geral"
                if (!dailyState.registeredMealTypes.contains(mealType)) {
                    pointsToAdd += GamificationRules.POINTS_MEAL_REGISTERED
                    val newMealTypes = dailyState.registeredMealTypes.toMutableSet()
                    newMealTypes.add(mealType)
                    dailyState = dailyState.copy(registeredMealTypes = newMealTypes)
                    messages.add("+${GamificationRules.POINTS_MEAL_REGISTERED} pontos! Refeição registrada.")
                }
            }
            is GamificationEvent.FoodAdded -> {
                if (dailyState.foodItemsPointedCount < GamificationRules.MAX_FOOD_POINTS_PER_DAY) {
                    pointsToAdd += GamificationRules.POINTS_FOOD_ADDED
                    dailyState = dailyState.copy(foodItemsPointedCount = dailyState.foodItemsPointedCount + 1)
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
                if (!dailyState.calorieGoalRewarded) {
                    pointsToAdd += GamificationRules.POINTS_CALORIE_GOAL_REACHED
                    dailyState = dailyState.copy(calorieGoalRewarded = true)
                    messages.add("Meta batida! +${GamificationRules.POINTS_CALORIE_GOAL_REACHED} pontos.")
                }
            }
            is GamificationEvent.AppUsedToday -> {
                // Trigger for streak logic only
            }
        }

        // 2. Update Streak
        val today = try { LocalDate.parse(date) } catch(_: Exception) { LocalDate.now() }
        val lastActiveStr = state.lastActiveDate
        
        if (lastActiveStr == null) {
            state = state.copy(currentStreak = 1, lastActiveDate = date)
            pointsToAdd += GamificationRules.POINTS_DAILY_STREAK
            dailyState = dailyState.copy(streakRewarded = true)
            messages.add("Primeiro dia ativo! +${GamificationRules.POINTS_DAILY_STREAK} pontos.")
        } else {
            val lastActive = LocalDate.parse(lastActiveStr)
            if (lastActive.isBefore(today)) {
                val daysBetween = ChronoUnit.DAYS.between(lastActive, today)
                if (daysBetween == 1L) {
                    if (!dailyState.streakRewarded) {
                        state = state.copy(
                            currentStreak = state.currentStreak + 1,
                            lastActiveDate = date
                        )
                        pointsToAdd += GamificationRules.POINTS_DAILY_STREAK
                        dailyState = dailyState.copy(streakRewarded = true)
                        messages.add("Sequência mantida: ${state.currentStreak} dias! +${GamificationRules.POINTS_DAILY_STREAK} pontos.")
                    }
                } else {
                    state = state.copy(currentStreak = 1, lastActiveDate = date)
                    if (!dailyState.streakRewarded) {
                        pointsToAdd += GamificationRules.POINTS_DAILY_STREAK
                        dailyState = dailyState.copy(streakRewarded = true)
                        messages.add("Nova sequência iniciada! +${GamificationRules.POINTS_DAILY_STREAK} pontos.")
                    }
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
                    "FIRST_MEAL" -> dailyState.registeredMealTypes.isNotEmpty()
                    "FIRST_WEIGHT" -> dailyState.weightRegistered
                    "FIRST_WORKOUT" -> dailyState.workoutRegistered
                    "THREE_DAY_STREAK" -> state.currentStreak >= 3
                    "SEVEN_DAY_STREAK" -> state.currentStreak >= 7
                    "CALORIE_GOAL_FIRST" -> dailyState.calorieGoalRewarded
                    else -> false
                }
                
                if (shouldUnlock) {
                    currentAchievements.add(achievement.id)
                    newlyUnlockedAchievements.add(achievement)
                }
            }
        }
        
        state = state.copy(unlockedAchievements = currentAchievements)

        // 5. Save and Return
        dataStore.saveGamificationState(state)
        dataStore.saveDailyState(clientId, dailyState)

        // Build Summarized Snackbar Message
        val snackbarMsg = when {
            levelUp -> "Você subiu para o nível $newLevel: ${GamificationRules.getLevelName(newLevel)}!"
            newlyUnlockedAchievements.isNotEmpty() -> "Conquista desbloqueada: ${newlyUnlockedAchievements.first().title}!"
            pointsToAdd > 0 -> {
                val actionMsg = messages.firstOrNull()?.substringAfter("! ") ?: "Pontos ganhos!"
                "+$pointsToAdd pontos! $actionMsg"
            }
            else -> null
        }

        return GamificationResult(
            pointsAdded = pointsToAdd,
            newTotalPoints = newTotalPoints,
            levelUp = levelUp,
            newLevel = newLevel,
            unlockedAchievements = newlyUnlockedAchievements,
            messages = messages,
            snackbarMessage = snackbarMsg
        )
    }
}
