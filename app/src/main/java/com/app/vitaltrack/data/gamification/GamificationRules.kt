package com.app.vitaltrack.data.gamification

object GamificationRules {
    const val POINTS_FIRST_MEAL_OF_DAY = 10
    const val POINTS_FOOD_ADDED = 2
    const val POINTS_WORKOUT_REGISTERED = 30
    const val POINTS_WEIGHT_REGISTERED = 10
    const val POINTS_CALORIE_GOAL_REACHED = 50
    const val POINTS_DAILY_STREAK = 10

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
            totalPoints >= 1000 -> 5
            totalPoints >= 500 -> 4
            totalPoints >= 250 -> 3
            totalPoints >= 100 -> 2
            else -> 1
        }
    }

    fun getLevelName(level: Int): String {
        return when (level) {
            1 -> "Iniciante"
            2 -> "Em movimento"
            3 -> "Focado"
            4 -> "Consistente"
            5 -> "Atleta VitalTrack"
            else -> "Iniciante"
        }
    }

    fun getNextLevelPoints(level: Int): Int {
        return when (level) {
            1 -> 100
            2 -> 250
            3 -> 500
            4 -> 1000
            else -> 1000
        }
    }

    fun getCurrentLevelBasePoints(level: Int): Int {
        return when (level) {
            1 -> 0
            2 -> 100
            3 -> 250
            4 -> 500
            5 -> 1000
            else -> 0
        }
    }

    fun calculateLevelProgress(totalPoints: Int): Float {
        val level = calculateLevel(totalPoints)
        if (level >= 5) return 1f

        val base = getCurrentLevelBasePoints(level)
        val next = getNextLevelPoints(level)
        val range = next - base
        if (range <= 0) return 1f
        
        val progress = (totalPoints - base).toFloat() / range.toFloat()

        return progress.coerceIn(0f, 1f)
    }
}
