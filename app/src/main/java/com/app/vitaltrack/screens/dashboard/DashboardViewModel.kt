package com.app.vitaltrack.screens.dashboard

import androidx.lifecycle.ViewModel
import com.app.vitaltrack.model.Meal
import com.app.vitaltrack.model.RecommendedFeature
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.WaterDrop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class DashboardUiState(
    val date: LocalDate = LocalDate.now(),
    val meals: List<Meal> = Meal.defaultMeals,
    val recommendedFeatures: List<RecommendedFeature> = listOf(
        RecommendedFeature("Lembrete de hidratação", Icons.Default.WaterDrop),
        RecommendedFeature("Contador de passos", Icons.AutoMirrored.Filled.DirectionsRun),
        RecommendedFeature("Relatório semanal", Icons.AutoMirrored.Filled.ShowChart),
        RecommendedFeature("Alertas de meta", Icons.Default.Notifications)
    ),
    val calorieGoal: Double = 2000.0
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
    private val dbDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun getFormattedDate(): String {
        return _uiState.value.date.format(dateFormatter)
    }
    
    fun getDbDate(): String {
        return _uiState.value.date.format(dbDateFormatter)
    }

    fun nextDay() {
        _uiState.update { it.copy(date = it.date.plusDays(1), meals = resetMeals(it.meals)) }
    }

    fun previousDay() {
        _uiState.update { it.copy(date = it.date.minusDays(1), meals = resetMeals(it.meals)) }
    }

    private fun resetMeals(meals: List<Meal>): List<Meal> {
        return meals.map { it.copy(calories = 0.0) }
    }

    fun addCaloriesToMeal(mealId: Long) {
        _uiState.update { state ->
            state.copy(
                meals = state.meals.map {
                    if (it.id == mealId) it.copy(calories = it.calories + 150.0) else it
                }
            )
        }
    }

    fun getTotalConsumed(): Double {
        return _uiState.value.meals.sumOf { it.calories }
    }
}
