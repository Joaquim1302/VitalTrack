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
            is GamificationEvent.WorkoutStarted -> event.clientId
            is GamificationEvent.WorkoutCompleted -> event.clientId
            is GamificationEvent.WorkoutSeriesCompleted -> event.clientId
            is GamificationEvent.AllPlannedExercisesCompleted -> event.clientId
            is GamificationEvent.ThreeWorkoutsInWeek -> event.clientId
        }

        val date = when (event) {
            is GamificationEvent.MealRegistered -> event.date
            is GamificationEvent.FoodAdded -> event.date
            is GamificationEvent.WeightRegistered -> event.date
            is GamificationEvent.WorkoutRegistered -> event.date
            is GamificationEvent.CalorieGoalReached -> event.date
            is GamificationEvent.AppUsedToday -> event.date
            is GamificationEvent.WorkoutStarted -> event.date
            is GamificationEvent.WorkoutCompleted -> event.date
            is GamificationEvent.WorkoutSeriesCompleted -> event.date
            is GamificationEvent.AllPlannedExercisesCompleted -> event.date
            is GamificationEvent.ThreeWorkoutsInWeek -> event.date
        }

        // Gera chave de idempotência para o evento
        val eventKey = when (event) {
            is GamificationEvent.WorkoutStarted -> "workout_started:${event.clientId}:${event.cdTreinoSessao}"
            is GamificationEvent.WorkoutCompleted -> "workout_completed:${event.clientId}:${event.cdTreinoSessao}"
            is GamificationEvent.WorkoutSeriesCompleted -> "workout_series_completed:${event.clientId}:${event.cdTreinoSessao}:${event.cdSerie}"
            is GamificationEvent.AllPlannedExercisesCompleted -> "all_planned_exercises_completed:${event.clientId}:${event.cdTreinoSessao}"
            is GamificationEvent.ThreeWorkoutsInWeek -> "three_workouts_week:${event.clientId}:${event.yearWeek}"
            else -> null // Eventos antigos usam DailyState para idempotência
        }

        var state = dataStore.getGamificationState(clientId)
        
        // Verifica se o evento já foi processado
        if (eventKey != null && state.processedEventKeys.contains(eventKey)) {
            return GamificationResult(newTotalPoints = state.totalPoints, newLevel = state.level)
        }

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
            // Novos eventos de musculação
            is GamificationEvent.WorkoutStarted -> {
                pointsToAdd += GamificationRules.POINTS_WORKOUT_STARTED
                messages.add("+${GamificationRules.POINTS_WORKOUT_STARTED} pontos! Treino iniciado.")
            }
            is GamificationEvent.WorkoutCompleted -> {
                pointsToAdd += GamificationRules.POINTS_WORKOUT_COMPLETED
                messages.add("+${GamificationRules.POINTS_WORKOUT_COMPLETED} pontos! Treino concluído.")
                // Também conta para o streak/uso diário se ainda não tiver
                if (!dailyState.workoutRegistered) {
                    dailyState = dailyState.copy(workoutRegistered = true)
                }
            }
            is GamificationEvent.WorkoutSeriesCompleted -> {
                pointsToAdd += GamificationRules.POINTS_WORKOUT_SERIES_COMPLETED
                messages.add("+${GamificationRules.POINTS_WORKOUT_SERIES_COMPLETED} pontos! Série registrada.")
            }
            is GamificationEvent.AllPlannedExercisesCompleted -> {
                pointsToAdd += GamificationRules.POINTS_ALL_PLANNED_EXERCISES_COMPLETED
                messages.add("+${GamificationRules.POINTS_ALL_PLANNED_EXERCISES_COMPLETED} pontos! Todos os exercícios concluídos.")
            }
            is GamificationEvent.ThreeWorkoutsInWeek -> {
                pointsToAdd += GamificationRules.POINTS_THREE_WORKOUTS_IN_WEEK
                messages.add("+${GamificationRules.POINTS_THREE_WORKOUTS_IN_WEEK} pontos! 3 treinos na semana!")
            }
        }

        // Adiciona a chave de evento processado para garantir idempotência
        if (eventKey != null) {
            val updatedProcessedKeys = state.processedEventKeys.toMutableSet()
            updatedProcessedKeys.add(eventKey)
            
            // Limpeza opcional: manter apenas as últimas 500 chaves para evitar crescimento infinito do DataStore
            if (updatedProcessedKeys.size > 500) {
                // Remove as chaves mais antigas (estratégia simples: remove 50 aleatórias ou por prefixo)
                // Aqui removemos apenas se não forem chaves semanais importantes
                val keysToRemove = updatedProcessedKeys.filter { !it.contains("_week:") }.take(50)
                updatedProcessedKeys.removeAll(keysToRemove.toSet())
            }
            
            state = state.copy(processedEventKeys = updatedProcessedKeys)
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
