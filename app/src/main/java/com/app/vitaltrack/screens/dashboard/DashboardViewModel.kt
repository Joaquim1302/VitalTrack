package com.app.vitaltrack.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.model.Meal
import com.app.vitaltrack.model.RecommendedFeature
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WaterDrop
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.repository.MealRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class DashboardUiState(
    val date: LocalDate = LocalDate.now(),
    val meals: List<Meal> = Meal.defaultMeals,
    val totalConsumed: Double = 0.0,
    val recommendedFeatures: List<RecommendedFeature> = listOf(
        RecommendedFeature("Lembrete de hidratação", Icons.Default.WaterDrop),
        RecommendedFeature("Exportar para Access", Icons.Default.Upload),
        RecommendedFeature("Relatório semanal", Icons.AutoMirrored.Filled.ShowChart),
        RecommendedFeature("Alertas de meta", Icons.Default.Notifications)
    ),
    val calorieGoal: Double = 2000.0
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MealRepository
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
    private val dbDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private var observationsJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MealRepository(db.mealDao(), db.refeicaoSalvaDao())
        observeMealCalories()
    }

    private fun observeMealCalories() {
        observationsJob?.cancel()
        observationsJob = _uiState
            .map { it.date.format(dbDateFormatter) }
            .distinctUntilChanged()
            .flatMapLatest { date ->
                repository.getCaloriesPerMeal(date, 1) // Mock clienteId = 1
            }
            .onEach { caloriesList ->
                _uiState.update { state ->
                    val updatedMeals = state.meals.map { meal ->
                        val mealCal = caloriesList.find { it.cdRefeicaoTp == meal.id.toInt() }
                        meal.copy(calories = mealCal?.totalCal ?: 0.0)
                    }
                    state.copy(
                        meals = updatedMeals,
                        totalConsumed = updatedMeals.sumOf { it.calories }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun getFormattedDate(): String {
        return _uiState.value.date.format(dateFormatter)
    }
    
    fun getDbDate(): String {
        return _uiState.value.date.format(dbDateFormatter)
    }

    fun nextDay() {
        _uiState.update { it.copy(date = it.date.plusDays(1)) }
    }

    fun previousDay() {
        _uiState.update { it.copy(date = it.date.minusDays(1)) }
    }

    fun reloadResumoDoDia() {
        // A observação reativa via Flow já cuida da atualização, 
        // mas podemos forçar um recarregamento se necessário.
    }
}
