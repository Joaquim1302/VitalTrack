package com.app.vitaltrack.data.gamification

object GamificationRules {
    const val POINTS_MEAL_REGISTERED = 10
    const val POINTS_FOOD_ADDED = 2
    const val POINTS_WORKOUT_REGISTERED = 30
    const val POINTS_WEIGHT_REGISTERED = 10
    const val POINTS_CALORIE_GOAL_REACHED = 50
    const val POINTS_DAILY_STREAK = 10
    
    // Novas regras de treino
    const val POINTS_WORKOUT_STARTED = 5
    const val POINTS_WORKOUT_COMPLETED = 30
    const val POINTS_WORKOUT_SERIES_COMPLETED = 2
    const val POINTS_ALL_PLANNED_EXERCISES_COMPLETED = 20
    const val POINTS_THREE_WORKOUTS_IN_WEEK = 50
    
    const val MAX_FOOD_POINTS_PER_DAY = 5

    val basicAchievements = listOf(
        Achievement(
            id = "FIRST_MEAL",
            title = "Primeiro Registro",
            description = "Registrou sua primeira refeição."
        ),
        Achievement(
            id = "FIRST_WEIGHT",
            title = "Primeiro Peso",
            description = "Registrou seu primeiro peso."
        ),
        Achievement(
            id = "FIRST_WORKOUT",
            title = "Primeiro Treino",
            description = "Registrou seu primeiro treino."
        ),
        Achievement(
            id = "THREE_DAY_STREAK",
            title = "3 Dias de Consistência",
            description = "Manteve uma sequência de 3 dias ativos."
        ),
        Achievement(
            id = "SEVEN_DAY_STREAK",
            title = "Semana Consistente",
            description = "Manteve uma sequência de 7 dias ativos."
        ),
        Achievement(
            id = "CALORIE_GOAL_FIRST",
            title = "Meta Batida",
            description = "Atingiu a meta calórica diária pela primeira vez."
        )
    )

    fun calculateLevel(totalPoints: Int): Int {
        return when {
            totalPoints >= 50000 -> 9
            totalPoints >= 30000 -> 8
            totalPoints >= 20000 -> 7
            totalPoints >= 12000 -> 6
            totalPoints >= 7000 -> 5
            totalPoints >= 3500 -> 4
            totalPoints >= 1000 -> 3
            totalPoints >= 500 -> 2
            else -> 1
        }
    }

    fun getLevelName(level: Int): String {
        return when (level) {
            1 -> "Iniciante"
            2 -> "Iniciado"
            3 -> "Determinado"
            4 -> "Consistente"
            5 -> "Atleta VitalTrack"
            6 -> "Amador Avançado"
            7 -> "Elite VitalTrack"
            8 -> "Rei da Montanha"
            9 -> "Escalador Profissional"
            else -> "Iniciante"
        }
    }

    fun getNextLevelPoints(level: Int): Int {
        return when (level) {
            1 -> 500
            2 -> 1000
            3 -> 3500
            4 -> 7000
            5 -> 12000
            6 -> 20000
            7 -> 30000
            8 -> 50000
            else -> 50000
        }
    }

    fun getCurrentLevelBasePoints(level: Int): Int {
        return when (level) {
            1 -> 0
            2 -> 500
            3 -> 1000
            4 -> 3500
            5 -> 7000
            6 -> 12000
            7 -> 20000
            8 -> 30000
            9 -> 50000
            else -> 0
        }
    }

    fun calculateLevelProgress(totalPoints: Int): Float {
        val level = calculateLevel(totalPoints)
        if (level >= 9) return 1f

        val base = getCurrentLevelBasePoints(level)
        val next = getNextLevelPoints(level)
        val range = next - base
        if (range <= 0) return 1f
        
        val progress = (totalPoints - base).toFloat() / range.toFloat()

        return progress.coerceIn(0f, 1f)
    }
}
