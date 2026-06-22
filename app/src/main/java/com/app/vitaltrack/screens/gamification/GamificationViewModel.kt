package com.app.vitaltrack.screens.gamification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.vitaltrack.data.gamification.GamificationRepository
import com.app.vitaltrack.data.gamification.GamificationState
import com.app.vitaltrack.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class GamificationViewModel(application: Application) : AndroidViewModel(application) {
    private val gamificationRepository = GamificationRepository(application)
    private val userPreferencesRepository = UserPreferencesRepository(application)

    private val _uiState = MutableStateFlow<GamificationState?>(null)
    val uiState: StateFlow<GamificationState?> = _uiState.asStateFlow()

    init {
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
                _uiState.value = state
            }
            .launchIn(viewModelScope)
    }
}
