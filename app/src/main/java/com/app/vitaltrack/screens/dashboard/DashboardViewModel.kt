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
import com.app.vitaltrack.data.gamification.GamificationEvent
import com.app.vitaltrack.data.gamification.GamificationRepository
import com.app.vitaltrack.data.gamification.GamificationState
import com.app.vitaltrack.database.AppDatabase
import com.app.vitaltrack.repository.MealRepository
import com.app.vitaltrack.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class DashboardUiState(
    val date: LocalDate = LocalDate.now(),
    val meals: List<Meal> = Meal.defaultMeals,
    val totalConsumed: Double = 0.0,
    val gamificationState: GamificationState? = null,
    val recommendedFeatures: List<RecommendedFeature> = listOf(
        RecommendedFeature("Lembrete de hidratação", Icons.Default.WaterDrop),
        RecommendedFeature("Exportar para Access", Icons.Default.Upload),
        RecommendedFeature("Relatório semanal", Icons.AutoMirrored.Filled.ShowChart),
        RecommendedFeature("Alertas de meta", Icons.Default.Notifications)
    ),
    val calorieGoal: Double = 2000.0
)

sealed interface DashboardEvent {
    data class ShowSnackbar(val message: String) : DashboardEvent
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MealRepository
    private val gamificationRepository = GamificationRepository(application)
    private val userPreferencesRepository = UserPreferencesRepository(application)
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = Channel<DashboardEvent>()
    val events: Flow<DashboardEvent> = _events.receiveAsFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", Locale.forLanguageTag("pt-BR"))
    private val dbDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private var observationsJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MealRepository(db.mealDao(), db.refeicaoSalvaDao())
        observePreferences()
        observeMealCalories()
        observeGamification()
    }

    private fun observeGamification() {
        userPreferencesRepository.userPreferencesFlow
            .map { it.clienteAtivoId }
            .distinctUntilChanged()
            .flatMapLatest { clientId ->
                if (clientId != null) {
                    gamificationRepository.getGamificationStateFlow(clientId)
                } else {
                    flowOf(null)
                }
            }
            .onEach { state ->
                _uiState.update { it.copy(gamificationState = state) }
            }
            .launchIn(viewModelScope)
    }

    private fun observePreferences() {
        userPreferencesRepository.userPreferencesFlow
            .onEach { prefs ->
                _uiState.update { it.copy(
                    calorieGoal = prefs.metaCalorias
                ) }

                if (prefs.clienteAtivoId == null) {
                    viewModelScope.launch(Dispatchers.IO) {
                        val db = AppDatabase.getDatabase(getApplication())
                        val clientes = db.clienteDao().listar()
                        if (clientes.isNotEmpty()) {
                            userPreferencesRepository.updateClienteAtivo(clientes[0].cdCliente, clientes[0].dsNome)
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeMealCalories() {
        observationsJob?.cancel()
        observationsJob = combine(
            _uiState.map { it.date.format(dbDateFormatter) }.distinctUntilChanged(),
            userPreferencesRepository.userPreferencesFlow.map { it.clienteAtivoId }.distinctUntilChanged()
        ) { date, clienteId ->
            date to (clienteId ?: 1L)
        }
        .flatMapLatest { (date, clienteId) ->
            repository.getCaloriesPerMeal(date, clienteId)
        }
        .onEach { caloriesList ->
            val totalConsumed = caloriesList.sumOf { it.totalCal }
            val state = _uiState.value
            
            // Check for Calorie Goal Reached event
            if (totalConsumed > 0 && totalConsumed <= state.calorieGoal) {
                val clientId = userPreferencesRepository.userPreferencesFlow.first().clienteAtivoId
                if (clientId != null) {
                    val result = gamificationRepository.registerEvent(
                        GamificationEvent.CalorieGoalReached(
                            clientId = clientId,
                            date = state.date.format(dbDateFormatter)
                        )
                    )
                    result.messages.forEach { msg ->
                        _events.send(DashboardEvent.ShowSnackbar(msg))
                    }
                }
            }

            _uiState.update { currentState ->
                val updatedMeals = currentState.meals.map { meal ->
                    val mealCal = caloriesList.find { it.cdRefeicaoTp == meal.id.toInt() }
                    meal.copy(calories = mealCal?.totalCal ?: 0.0)
                }
                currentState.copy(
                    meals = updatedMeals,
                    totalConsumed = totalConsumed
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
